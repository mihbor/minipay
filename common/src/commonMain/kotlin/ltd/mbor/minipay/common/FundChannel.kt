package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.serialization.json.jsonArray
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel

enum class FundChannelEvent{
  SCRIPTS_DEPLOYED, FUNDING_TX_CREATED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, SIGS_RECEIVED, CHANNEL_FUNDED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

suspend fun ChannelService.prepareFundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  val myAddress = mds.getAddress().address
  val fundingTxId = fundingTx(myAmount, tokenId)
  event(FUNDING_TX_CREATED, null)
  
  val triggerTxId = mds.signFloatingTx(myKeys.trigger, multisigScriptAddress, tokenId, mapOf(99 to "0"), myAmount+theirAmount to eltooScriptAddress)
  event(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = mds.signFloatingTx(myKeys.settle, eltooScriptAddress, tokenId, mapOf(99 to "0"), myAmount to myAddress, theirAmount to theirAddress)
  event(SETTLEMENT_TX_SIGNED, null)
  
  val signedTriggerTx = mds.exportTx(triggerTxId)
  val signedSettlementTx = mds.exportTx(settlementTxId)
  val unsignedFundingTx = mds.exportTx(fundingTxId)
  
  val channelId =
    storage.insertChannel(tokenId, myAmount, theirAmount, myKeys, theirKeys, signedTriggerTx, signedSettlementTx, timeLock, multisigScriptAddress, eltooScriptAddress, myAddress, theirAddress)
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
  event(CHANNEL_PERSISTED, channel)
  
  transport.publish(
    channelKey(theirKeys, tokenId),
    listOf(timeLock, myKeys.trigger, myKeys.update, myKeys.settle, signedTriggerTx, signedSettlementTx, unsignedFundingTx).joinToString(";")
  )
  event(CHANNEL_PUBLISHED, channel)
  
  return channel
}

suspend fun ChannelService.fundingTx(amount: BigDecimal, tokenId: String): Int {
  val txnId = newTxId()
  val (inputs, change) = mds.inputsWithChange(tokenId, amount)

  val txncreator = buildString {
    appendLine("txncreate id:$txnId;")
    inputs.forEach { appendLine("txninput id:$txnId coinid:${it.coinId};") }
    change.forEach { appendLine("txnoutput id:$txnId amount:${it.amount.toPlainString()} tokenid:${it.tokenId} address:${it.address};") }
  }.trim()

  mds.cmd(txncreator)
  return txnId
}

suspend fun Channel.commitFund(
  key: String,
  triggerTx: String,
  settlementTx: String,
  fundingTx: String,
  theirInputCoins: List<String>,
  theirInputScripts: List<String>
): Channel {
  MDS.importTx(newTxId(), triggerTx)
  MDS.importTx(newTxId(), settlementTx)
  val fundingTxId = newTxId()
  MDS.importTx(fundingTxId, fundingTx).outputs
  theirInputCoins.forEach { MDS.importCoin(it) }
  theirInputScripts.forEach { MDS.newScript(it) }
  
  val txncreator = buildString {
    appendLine("txnsign id :$fundingTxId publickey:$key;")
    appendLine("txnpost id :$fundingTxId auto:true;")
    append("txndelete id :$fundingTxId;")
  }
  val result = MDS.cmd(txncreator)!!.jsonArray
  val status = result.find{ it.jsonString("command") == "txnpost" }!!.jsonString("status")
  log("txnpost status: $status")
  
  return if (status.toBoolean()) {
    storage.updateChannel(this, triggerTx, settlementTx)
  } else this
}
