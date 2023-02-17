package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.window
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.MinimaException
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

  var channel = prepareFundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock, multisigScriptAddress, eltooScriptAddress, onEvent)
  
  channelKey(channel.my.keys, channel.tokenId).subscribe({ it, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, it)
  }) {
    val (triggerTx, settlementTx, fundingTx) = it
    val (theirInputCoins, theirInputScripts) = it.subList(3, it.size)
      .let{ it.takeUnless { it.isEmpty() }?.chunked(it.size/2) ?: listOf(emptyList(), emptyList()) }
    onEvent(SIGS_RECEIVED, channel)
    try {
      channel = channel.commitFund("auto", triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts)
      onEvent(CHANNEL_FUNDED, channel)
      channels.put(channel)
    } catch (e: MinimaException) {
      window.alert("MinimaException: ${e.message}")
    }
    channel.id
  }
}
