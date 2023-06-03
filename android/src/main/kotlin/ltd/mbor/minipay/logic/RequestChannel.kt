package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.transport.MaximaTransport

fun requestChannel(
  myAddress: String,
  myKeys: Channel.Keys,
  tokenId: String,
  myAmount: BigDecimal,
  maximaContact: Contact?,
  onEvent: (RequestChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  if (maximaContact != null) scope.launch{
    val transport = MaximaTransport(maximaContact.publicKey)
    transport.publish(
      channelKey(myKeys, tokenId),
      "INVITE;" + myAmount.toPlainString() + ";" + myAddress
    )
  } else channelKey(myKeys, tokenId).subscribe({ channel, isAck ->
    onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
  }) {
    if (it.first() == "ACCEPTED") it.drop(1).let {
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
      val channel = channelService.requestChannel(
        myKeys,
        theirKeys,
        myAddress,
        theirAddress,
        myAmount,
        tokenId,
        timeLock,
        multisigScriptAddress,
        eltooScriptAddress,
        triggerTx,
        settlementTx,
        fundingTx,
        onEvent
      )
      channels.put(channel)
      channel.id
    } else null
  }
}
