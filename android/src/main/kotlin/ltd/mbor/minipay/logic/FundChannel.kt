package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel

suspend fun fundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  onEvent: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  multisigScriptAddress = MDS.newScript(triggerScript(myKeys.trigger, theirKeys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, myKeys.update, theirKeys.update, myKeys.settle, theirKeys.settle)).address
  onEvent(SCRIPTS_DEPLOYED, null)

  val channel = channelService.prepareFundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock, multisigScriptAddress, eltooScriptAddress, onEvent)

  channel.subscribe({ it, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, it)
  }) {
    val (triggerTx, settlementTx, fundingTx) = it
    val (theirInputCoins, theirInputScripts) = it.subList(3, it.size)
      .let{ it.takeUnless { it.isEmpty() }?.chunked(it.size/2) ?: listOf(emptyList(), emptyList()) }
    onEvent(SIGS_RECEIVED, channel)
    with(channelService) {
      channel.commitFund(triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts).also {
        onEvent(CHANNEL_FUNDED, it)
        channels.put(it)
      }
    }
  }
}
