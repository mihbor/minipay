package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.serialization.json.jsonArray
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.logic.FundChannelEvent.*
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.view

enum class FundChannelEvent{
  SCRIPTS_DEPLOYED, FUNDING_TX_CREATED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, SIGS_RECEIVED, CHANNEL_FUNDED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

suspend fun fundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  var channel = prepareFundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock, event)
  
  subscribe(channelKey(myKeys, tokenId)).onEach { msg ->
    log("tx msg: $msg")
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channel = channel.update(isAck, updateTxText = splits[1], settleTxText = splits[2])
      event(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      val updateTxId = newTxId()
      MDS.importTx(updateTxId, updateTxText)
      val settleTxId = newTxId()
      val settleTx = MDS.importTx(settleTxId, settleTxText)
      val channelBalance = settleTx.outputs.first{ it.address == channel.my.address }.tokenAmount to settleTx.outputs.first{ it.address == channel.their.address }.tokenAmount
      val newSequenceNumber = settleTx.state.first { it.port == 99 }.data.toInt()
      if (newSequenceNumber > channel.sequenceNumber) {
        events += PaymentRequestSent(
          channel,
          updateTxId,
          settleTxId,
          newSequenceNumber,
          channelBalance,
          false
        )
        view = "Channel events"
      } else log("Stale update $newSequenceNumber received for channel ${channel.id} at ${channel.sequenceNumber}")
    } else {
      val (triggerTx, settlementTx, fundingTx) = splits
      val (theirInputCoins, theirInputScripts) = splits.subList(3, splits.size).let{ it.chunked(it.size/2) }
      event(SIGS_RECEIVED, channel)
      channel = channel.commitFund("auto", tokenId, myAmount, triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts)
      event(CHANNEL_FUNDED, channel)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}

suspend fun fundingTx(amount: BigDecimal, tokenId: String): Int {
  val txnId = newTxId()
  val (inputs, change) = MDS.inputsWithChange(tokenId, amount)
  
  val txncreator = buildString {
    appendLine("txncreate id:$txnId;")
    inputs.forEach { appendLine("txninput id:$txnId coinid:${it.coinId};") }
    change.forEach { appendLine("txnoutput id:$txnId amount:${it.amount.toPlainString()} tokenid:${it.tokenId} address:${it.address};") }
  }.trim()
  
  MDS.cmd(txncreator)
  return txnId
}

suspend fun prepareFundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  val myAddress = MDS.getAddress().address
  multisigScriptAddress = MDS.newScript(triggerScript(myKeys.trigger, theirKeys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, myKeys.update, theirKeys.update, myKeys.settle, theirKeys.settle)).address
  event(SCRIPTS_DEPLOYED, null)
  
  val fundingTxId = fundingTx(myAmount, tokenId)
  event(FUNDING_TX_CREATED, null)
  
  val triggerTxId = signFloatingTx(myKeys.trigger, multisigScriptAddress, mapOf(99 to "0"), tokenId, myAmount+theirAmount to eltooScriptAddress)
  event(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = signFloatingTx(myKeys.settle, eltooScriptAddress, mapOf(99 to "0"), tokenId, myAmount to myAddress, theirAmount to theirAddress)
  event(SETTLEMENT_TX_SIGNED, null)
  
  val signedTriggerTx = MDS.exportTx(triggerTxId)
  val signedSettlementTx = MDS.exportTx(settlementTxId)
  val unsignedFundingTx = MDS.exportTx(fundingTxId)
  
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

  publish(
    channelKey(theirKeys, tokenId),
    listOf(timeLock, myKeys.trigger, myKeys.update, myKeys.settle, signedTriggerTx, signedSettlementTx, unsignedFundingTx).joinToString(";")
  )
  event(CHANNEL_PUBLISHED, channel)
  
  return channel
}

suspend fun Channel.commitFund(
  key: String,
  tokenId: String,
  myAmount: BigDecimal,
  triggerTx: String,
  settlementTx: String,
  fundingTx: String,
  theirInputCoins: List<String>,
  theirInputScripts: List<String>
): Channel {
  MDS.importTx(newTxId(), triggerTx)
  MDS.importTx(newTxId(), settlementTx)
  val fundingTxId = newTxId()
  val theirBalance = MDS.importTx(fundingTxId, fundingTx).outputs
    .find { it.address == multisigScriptAddress && it.tokenId == tokenId }!!.tokenAmount - myAmount
  theirInputCoins.forEach { MDS.importCoin(it) }
  theirInputScripts.forEach { MDS.newScript(it) }
  MDS.logging=true
  val txncreator = buildString {
    appendLine("txnsign id :$fundingTxId publickey:$key;")
    appendLine("txnpost id :$fundingTxId auto:true;")
    append("txndelete id :$fundingTxId;")
  }
  val result = MDS.cmd(txncreator)!!.jsonArray
  val status = result.find{ it.jsonString("command") == "txnpost" }!!.jsonString("status")
  log("txnpost status: $status")
  
  return if (status.toBoolean()) {
    updateChannel(this, triggerTx, settlementTx)
  } else this
}
