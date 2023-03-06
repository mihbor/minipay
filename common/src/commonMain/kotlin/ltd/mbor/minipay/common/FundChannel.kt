package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.exportTx
import ltd.mbor.minimak.inputsWithChange
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel

enum class FundChannelEvent{
  SCRIPTS_DEPLOYED, FUNDING_TX_CREATED, TRIGGER_TX_SIGNED, SETTLEMENT_TX_SIGNED, CHANNEL_PERSISTED, CHANNEL_PUBLISHED, SIGS_RECEIVED, CHANNEL_FUNDED, CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED
}

suspend fun ChannelService.prepareFundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  myAddress: String,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  val fundingTxId = fundingTx(myAmount, tokenId)
  event(FUNDING_TX_CREATED, null)
  
  val triggerTxId = mds.signFloatingTx(myKeys.trigger, multisigScriptAddress, tokenId, mapOf(99 to "0"), myAmount+theirAmount to eltooScriptAddress)
  event(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = mds.signFloatingTx(myKeys.settle, eltooScriptAddress, tokenId, mapOf(99 to "0"), myAmount to myAddress, theirAmount to theirAddress)
  event(SETTLEMENT_TX_SIGNED, null)
  
  val signedTriggerTx = mds.exportTx(triggerTxId)
  val signedSettlementTx = mds.exportTx(settlementTxId)
  val unsignedFundingTx = mds.exportTx(fundingTxId)
  
  val channel = storage.insertChannel(tokenId, myAmount, theirAmount, myKeys, theirKeys, signedTriggerTx, signedSettlementTx, timeLock, multisigScriptAddress, eltooScriptAddress, myAddress, theirAddress)

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
