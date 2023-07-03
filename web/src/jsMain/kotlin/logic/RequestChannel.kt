package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.transport.MaximaTransport

var inviteSent: ChannelInvite? = null
var requestedChannel by mutableStateOf<Channel?>(null)
var onChannelRequested: (RequestChannelEvent, Channel?) -> Unit = { _, _ -> }

fun requestChannel(
  myInvite: ChannelInvite,
  maximaContact: Contact?,
  onEvent: (RequestChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  inviteSent = myInvite
  onChannelRequested = onEvent
  if (maximaContact != null) scope.launch{
    val transport = MaximaTransport(maximaContact.publicKey)
    transport.publish(
      channelKey(myInvite.keys, myInvite.tokenId),
      "INVITE;" + myInvite.balance.toPlainString() + ";" + myInvite.address
    )
  } else channelKey(myInvite.keys, myInvite.tokenId).subscribe({ channel, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
  }) {
    if (it.first() == "ACCEPTED") it.drop(1).let {
      requestChannelAccepted(it, myInvite, null)
    } else null
  }
}

suspend fun requestChannelAccepted(
  it: List<String>,
  myInvite: ChannelInvite,
  maximaPK: String?
): Uuid {
  val timeLock = it[0].toInt()
  val theirKeys = Channel.Keys(it[1], it[2], it[3])
  val triggerTx = it[4]
  val settlementTx = it[5]
  val fundingTx = it[6]
  val theirAddress = it[7]
  onChannelRequested(SIGS_RECEIVED, null)
  multisigScriptAddress = MDS.newScript(triggerScript(theirKeys.trigger, myInvite.keys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, theirKeys.update, myInvite.keys.update, theirKeys.settle, myInvite.keys.settle)).address
  onChannelRequested(SCRIPTS_DEPLOYED, null)
  val channel = channelService.requestedChannelAccepted(
    myInvite.keys,
    theirKeys,
    myInvite.address,
    theirAddress,
    myInvite.balance,
    myInvite.tokenId,
    timeLock,
    multisigScriptAddress,
    eltooScriptAddress,
    triggerTx,
    settlementTx,
    fundingTx,
    maximaPK,
    onChannelRequested
  )
  channels.put(channel)
  return channel.id
}
