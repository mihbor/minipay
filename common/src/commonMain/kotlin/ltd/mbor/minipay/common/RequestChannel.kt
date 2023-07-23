package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.transport.MaximaTransport

enum class RequestChannelEvent{
  SCRIPTS_DEPLOYED, SIGS_RECEIVED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

suspend fun ChannelService.requestedChannelAccepted(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  myAddress: String,
  theirAddress: String,
  myAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  triggerTx: String,
  settlementTx: String,
  fundingTx: String,
  maximaPK: String?,
  onEvent: (RequestChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  
  val triggerTxId = newTxId()
  mds.importTx(triggerTxId, triggerTx)
  val signedTriggerTx = mds.signAndExportTx(triggerTxId, myKeys.trigger)
  onEvent(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = newTxId()
  mds.importTx(settlementTxId, settlementTx)
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
  
  val channel = storage.insertChannel(
    name = maximaPK?.let { pk -> mds.getContacts().firstOrNull { it.publicKey == pk }?.extraData?.name },
    tokenId = tokenId,
    myBalance = myAmount,
    theirBalance = theirAmount,
    myKeys = myKeys,
    theirKeys = theirKeys,
    signedTriggerTx = signedTriggerTx,
    signedSettlementTx = signedSettlementTx,
    timeLock = timeLock,
    multisigScriptAddress = multisigScriptAddress,
    eltooScriptAddress = eltooScriptAddress,
    myAddress = myAddress,
    theirAddress = theirAddress,
    maximaPK = maximaPK
  )
  onEvent(CHANNEL_PERSISTED, channel)

  val (exportedCoins, scripts) = exportedCoinsAndScripts.unzip()
  val transport = maximaPK?.let(::MaximaTransport) ?: this.transport
  transport.publish(
    channelKey(theirKeys, tokenId),
    (listOf("CONFIRMED", signedTriggerTx, signedSettlementTx, signedFundingTx) + exportedCoins + scripts).joinToString(";")
  )
  onEvent(CHANNEL_PUBLISHED, channel)
  
  return channel
}
