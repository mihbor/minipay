package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite

suspend fun fundChannel(
  invite: ChannelInvite,
  myKeys: Channel.Keys,
  myAddress: String,
  myAmount: BigDecimal,
  timeLock: Int,
  onEvent: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  multisigScriptAddress = MDS.newScript(triggerScript(myKeys.trigger, invite.keys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, myKeys.update, invite.keys.update, myKeys.settle, invite.keys.settle)).address
  onEvent(SCRIPTS_DEPLOYED, null)

  val channel = channelService.prepareFundChannel(invite, myKeys, myAddress, myAmount, timeLock, multisigScriptAddress, eltooScriptAddress, onEvent)

  channel.subscribe({ it, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, it)
  }) {
    if (it.first() == "CONFIRMED") {
      val (_, triggerTx, settlementTx, fundingTx) = it
      val (theirInputCoins, theirInputScripts) = it.subList(3, it.size)
        .let { it.takeUnless { it.isEmpty() }?.chunked(it.size / 2) ?: listOf(emptyList(), emptyList()) }
      onEvent(SIGS_RECEIVED, channel)
      with(channelService) {
        channel.commitFund(triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts).also {
          onEvent(CHANNEL_FUNDED, it)
          channels.put(it)
        }
      }
    }
  }
}
