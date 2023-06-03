package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.window
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.MinimaException
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite

var channelToFund by mutableStateOf<Channel?>(null)
var onFundChannel: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }

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
  onFundChannel = onEvent

  if (invite.maximaPK == null) channel.subscribe({ it, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, it)
  }) {
    if (it.first() == "CONFIRMED") it.drop(1).let {
      fundChannelConfirmed(it, channel)
    }
  }
}

suspend fun fundChannelConfirmed(
  it: List<String>,
  channel: Channel,
) {
  val (triggerTx, settlementTx, fundingTx) = it
  val (theirInputCoins, theirInputScripts) = it.drop(3)
    .let { it.takeUnless { it.isEmpty() }?.chunked(it.size / 2) ?: listOf(emptyList(), emptyList()) }
  onFundChannel(SIGS_RECEIVED, channel)
  try {
    with(channelService) {
      channel.commitFund(triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts, "auto").also {
        onFundChannel(CHANNEL_FUNDED, it)
        channels.put(it)
      }
    }
  } catch (e: MinimaException) {
    window.alert("MinimaException: ${e.message}")
  }
}
