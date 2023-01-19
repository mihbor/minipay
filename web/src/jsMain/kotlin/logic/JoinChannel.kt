package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import logic.JoinChannelEvent.*
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.*
import scope
import view

enum class JoinChannelEvent{
  SCRIPTS_DEPLOYED, SIGS_RECEIVED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

fun joinChannel(
  myAddress: String,
  myKeys: Channel.Keys,
  tokenId: String,
  amount: BigDecimal,
  event: (JoinChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  var channel: Channel? = null
  subscribe(channelKey(myKeys, tokenId)).onEach { msg ->
    log("tx msg: $msg")
    
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channel = channel!!.update(isAck, updateTxText = splits[1], settleTxText = splits[2])
      event(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      val updateTxId = newTxId()
      MDS.importTx(updateTxId, updateTxText)
      val settleTxId = newTxId()
      val settleTx = MDS.importTx(settleTxId, settleTxText)
      val channelBalance = settleTx.outputs.first{ it.address == channel!!.my.address }.tokenAmount to settleTx.outputs.first{ it.address == channel!!.their.address }.tokenAmount
      val newSequenceNumber = settleTx.state.first { it.port == 99 }.data.toInt()
      if (newSequenceNumber > channel!!.sequenceNumber) {
        events += PaymentRequestSent(
          channel!!,
          updateTxId,
          settleTxId,
          newSequenceNumber,
          channelBalance,
        )
        view = "Channel events"
      } else log("Stale update $newSequenceNumber received for channel ${channel?.id} at ${channel?.sequenceNumber}")
    } else {
      val timeLock = splits[0].toInt()
      val theirKeys = Channel.Keys(splits[1], splits[2], splits[3])
      val triggerTx = splits[4]
      val settlementTx = splits[5]
      val fundingTx = splits[6]
      event(SIGS_RECEIVED, null)
      joinChannel(myAddress, myKeys, theirKeys, tokenId, amount, triggerTx, settlementTx, fundingTx, timeLock, event)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}

suspend fun joinChannel(
  myAddress: String,
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  tokenId: String,
  myAmount: BigDecimal,
  triggerTx: String,
  settlementTx: String,
  fundingTx: String,
  timeLock: Int,
  event: (JoinChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  multisigScriptAddress = MDS.newScript(triggerScript(theirKeys.trigger, myKeys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, theirKeys.update, myKeys.update, theirKeys.settle, myKeys.settle)).address
  event(SCRIPTS_DEPLOYED, null)
  
  val triggerTxId = newTxId()
  MDS.importTx(triggerTxId, triggerTx)
  val signedTriggerTx = signAndExportTx(triggerTxId, myKeys.trigger)
  event(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = newTxId()
  val importedSettlementTx = MDS.importTx(settlementTxId, settlementTx)
  val theirAddress = importedSettlementTx.outputs.first().address
  val signedSettlementTx = signAndExportTx(settlementTxId, myKeys.settle)
  event(SETTLEMENT_TX_SIGNED, null)
  
  val fundingTxId = newTxId()
  val importedFundingTx = MDS.importTx(fundingTxId, fundingTx)
  val theirAmount = importedFundingTx.inputs.filter { it.tokenId == tokenId }.sumOf { it.tokenAmount } -
    importedFundingTx.outputs.filter { it.tokenId == tokenId }.sumOf { it.tokenAmount }
  log("their amount: ${theirAmount.toPlainString()}")

  val (signedFundingTx, exportedCoinsAndScripts) = if (myAmount > ZERO) {
    val (inputs, change) = MDS.inputsWithChange(tokenId, myAmount)
  
    val txncreator = buildString {
      inputs.forEach { appendLine("txninput id:$fundingTxId coinid:${it.coinId};") }
      change.forEach { appendLine("txnoutput id:$fundingTxId amount:${it.amount.toPlainString()} tokenid:${it.tokenId} address:${it.address};") }
      append("txnoutput id:$fundingTxId amount:${(myAmount + theirAmount).toPlainString()} tokenid:$tokenId address:$multisigScriptAddress;")
    }
    MDS.cmd(txncreator)
    val scripts = MDS.getScripts().associate { it.address to it.script }
    signAndExportTx(fundingTxId, "auto") to inputs.map { MDS.exportCoin(it.coinId) to scripts[it.address] }
  } else {
    MDS.cmd("txnoutput id:$fundingTxId amount:${theirAmount.toPlainString()} tokenid:$tokenId address:$multisigScriptAddress;")
    Pair(signAndExportTx(fundingTxId, "auto"), emptyList())
  }
  
  val channelId = insertChannel(tokenId, myAmount, theirAmount, myKeys, theirKeys, signedTriggerTx, signedSettlementTx, timeLock, multisigScriptAddress, eltooScriptAddress, myAddress, theirAddress)
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
    updatedAt = Clock.System.now()
  )
  event(CHANNEL_PERSISTED, channel)

  val (exportedCoins, scripts) = exportedCoinsAndScripts.unzip()
  publish(
    channelKey(theirKeys, tokenId),
    (listOf(signedTriggerTx, signedSettlementTx, signedFundingTx) + exportedCoins + scripts).joinToString(";")
  )
  event(CHANNEL_PUBLISHED, channel)

  return channel
}
