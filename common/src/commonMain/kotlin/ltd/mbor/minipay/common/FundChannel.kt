package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.exportTx
import ltd.mbor.minimak.getContacts
import ltd.mbor.minimak.inputsWithChange
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.transport.MaximaTransport

enum class FundChannelEvent{
  SCRIPTS_DEPLOYED,
  FUNDING_TX_CREATED,
  TRIGGER_TX_SIGNED,
  SETTLEMENT_TX_SIGNED,
  CHANNEL_PERSISTED,
  CHANNEL_PUBLISHED,
  SIGS_RECEIVED,
  CHANNEL_FUNDED,
  CHANNEL_UPDATED,
  CHANNEL_UPDATED_ACKED
}

suspend fun ChannelService.prepareFundChannel(
  invite: ChannelInvite,
  myKeys: Channel.Keys,
  myAddress: String,
  myAmount: BigDecimal,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
): Channel {
  val fundingTxId = fundingTx(myAmount, invite.tokenId)
  event(FUNDING_TX_CREATED, null)
  
  val triggerTxId = mds.signFloatingTx(myKeys.trigger, multisigScriptAddress, invite.tokenId, mapOf(99 to "0"), myAmount+invite.balance to eltooScriptAddress)
  event(TRIGGER_TX_SIGNED, null)
  
  val settlementTxId = mds.signFloatingTx(myKeys.settle, eltooScriptAddress, invite.tokenId, mapOf(99 to "0"), myAmount to myAddress, invite.balance to invite.address)
  event(SETTLEMENT_TX_SIGNED, null)
  
  val signedTriggerTx = mds.exportTx(triggerTxId)
  val signedSettlementTx = mds.exportTx(settlementTxId)
  val unsignedFundingTx = mds.exportTx(fundingTxId)
  
  val channel = storage.insertChannel(
    name = invite.maximaPK?.let { pk -> mds.getContacts().firstOrNull { it.publicKey == pk }?.extraData?.name },
    tokenId = invite.tokenId,
    myBalance = myAmount,
    theirBalance = invite.balance,
    myKeys = myKeys,
    theirKeys = invite.keys,
    signedTriggerTx = signedTriggerTx,
    signedSettlementTx = signedSettlementTx,
    timeLock = timeLock,
    multisigScriptAddress = multisigScriptAddress,
    eltooScriptAddress = eltooScriptAddress,
    myAddress = myAddress,
    theirAddress = invite.address,
    maximaPK = invite.maximaPK
  )

  event(CHANNEL_PERSISTED, channel)
  val transport = invite.maximaPK?.let(::MaximaTransport) ?: this.transport
  transport.publish(
    channelKey(invite.keys, invite.tokenId),
    listOf("ACCEPTED", timeLock, myKeys.trigger, myKeys.update, myKeys.settle, signedTriggerTx, signedSettlementTx, unsignedFundingTx, myAddress).joinToString(";")
  )
  event(CHANNEL_PUBLISHED, channel)
  
  return channel
}

suspend fun ChannelService.fundingTx(amount: BigDecimal, tokenId: String): Int {
  val txnId = newTxId()
  val (inputs, change) = if (amount > ZERO) mds.inputsWithChange(tokenId, amount) else emptyList<Coin>() to emptyList()

  val txncreator = buildString {
    appendLine("txncreate id:$txnId;")
    inputs.forEach { appendLine("txninput id:$txnId coinid:${it.coinId};") }
    change.forEach { appendLine("txnoutput id:$txnId amount:${it.amount.toPlainString()} tokenid:${it.tokenId} address:${it.address};") }
  }.trim()

  mds.cmd(txncreator)
  return txnId
}
