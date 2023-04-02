package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.model.Channel

fun requestChannel(
  myAddress: String,
  myKeys: Channel.Keys,
  tokenId: String,
  amount: BigDecimal,
  onEvent: (RequestChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  channelKey(myKeys, tokenId).subscribe({ channel, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
  }) {
    val timeLock = it[0].toInt()
    val theirKeys = Channel.Keys(it[1], it[2], it[3])
    val triggerTx = it[4]
    val settlementTx = it[5]
    val fundingTx = it[6]
    val theirAddress = it[7]
    onEvent(SIGS_RECEIVED, null)
    multisigScriptAddress = MDS.newScript(triggerScript(theirKeys.trigger, myKeys.trigger)).address
    eltooScriptAddress = MDS.newScript(eltooScript(timeLock, theirKeys.update, myKeys.update, theirKeys.settle, myKeys.settle)).address
    onEvent(SCRIPTS_DEPLOYED, null)
    val channel = channelService.requestChannel(myKeys, theirKeys, myAddress, theirAddress, amount, tokenId, timeLock, multisigScriptAddress, eltooScriptAddress, triggerTx, settlementTx, fundingTx, onEvent)
    channels.put(channel)
    channel.id
  }
}
