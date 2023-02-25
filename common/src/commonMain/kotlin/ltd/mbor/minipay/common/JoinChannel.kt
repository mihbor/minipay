package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.datetime.Clock
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.JoinChannelEvent.*
import ltd.mbor.minipay.common.model.Channel

enum class JoinChannelEvent{
  SCRIPTS_DEPLOYED, SIGS_RECEIVED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

suspend fun ChannelService.joinChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  myAddress: String,
  myAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  triggerTx: String,
  settlementTx: String,
  fundingTx: String,
  onEvent: (JoinChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  
  val triggerTxId = newTxId()
  mds.importTx(triggerTxId, triggerTx)
  val signedTriggerTx = mds.signAndExportTx(triggerTxId, myKeys.trigger)
  onEvent(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = newTxId()
  val importedSettlementTx = mds.importTx(settlementTxId, settlementTx)
  val theirAddress = importedSettlementTx.outputs.first().address
  val signedSettlementTx = mds.signAndExportTx(settlementTxId, myKeys.settle)
  onEvent(SETTLEMENT_TX_SIGNED, null)
  
  val fundingTxId = newTxId()
  val importedFundingTx = mds.importTx(fundingTxId, fundingTx)
  val theirAmount = importedFundingTx.inputs.filter { it.tokenId == tokenId }.sumOf { it.tokenAmount } -
    importedFundingTx.outputs.filter { it.tokenId == tokenId }.sumOf { it.tokenAmount }
  log("their amount: ${theirAmount.toPlainString()}")
  
  val (signedFundingTx, exportedCoinsAndScripts) = if (myAmount > ZERO) {
    val (inputs, change) = mds.inputsWithChange(tokenId, myAmount)
    
    val txncreator = buildString {
      inputs.forEach { appendLine("txninput id:$fundingTxId coinid:${it.coinId};") }
      change.forEach { appendLine("txnoutput id:$fundingTxId amount:${it.amount.toPlainString()} tokenid:${it.tokenId} address:${it.address};") }
      append("txnoutput id:$fundingTxId amount:${(myAmount + theirAmount).toPlainString()} tokenid:$tokenId address:$multisigScriptAddress;")
    }
    mds.cmd(txncreator)
    val scripts = mds.getScripts().associate { it.address to it.script }
    mds.signAndExportTx(fundingTxId, "auto") to inputs.map { mds.exportCoin(it.coinId) to scripts[it.address] }
  } else {
    mds.cmd("txnoutput id:$fundingTxId amount:${theirAmount.toPlainString()} tokenid:$tokenId address:$multisigScriptAddress;")
    Pair(mds.signAndExportTx(fundingTxId, "auto"), emptyList())
  }
  
  val channelId = storage.insertChannel(tokenId, myAmount, theirAmount, myKeys, theirKeys, signedTriggerTx, signedSettlementTx, timeLock, multisigScriptAddress, eltooScriptAddress, myAddress, theirAddress)
  val channel = Channel(
    id = channelId,
    sequenceNumber = 0,
    status = "OFFERED",
    tokenId = tokenId,
    my = Channel.Side(
      balance = myAmount,
      address = myAddress,
      keys = myKeys
    ),
    their = Channel.Side(
      balance = theirAmount,
      address = theirAddress,
      keys = theirKeys
    ),
    triggerTx = signedTriggerTx,
    settlementTx = signedSettlementTx,
    timeLock = timeLock,
    eltooAddress = eltooScriptAddress,
    multiSigAddress = multisigScriptAddress,
    updatedAt = Clock.System.now()
  )
  onEvent(CHANNEL_PERSISTED, channel)
  
  val (exportedCoins, scripts) = exportedCoinsAndScripts.unzip()
  transport.publish(
    channelKey(theirKeys, tokenId),
    (listOf(signedTriggerTx, signedSettlementTx, signedFundingTx) + exportedCoins + scripts).joinToString(";")
  )
  onEvent(CHANNEL_PUBLISHED, channel)
  
  return channel
}
