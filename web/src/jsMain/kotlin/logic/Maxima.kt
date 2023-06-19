package logic

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.decodeHex
import ltd.mbor.minimak.jsonString
import ltd.mbor.minimak.log
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.ChannelInviteReceived
import ltd.mbor.minipay.common.model.Transport.MAXIMA
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.common.storage
import view

fun onMessage(msg: JsonElement) {
  val data = msg.jsonString("data").substring(2).decodeHex().decodeToString()
  val senderPK = msg.jsonString("from")
  log("received: $data from $senderPK")
  val (key, payload) = data.split(":")
  payload.split(";").let {
    val event = it.first()
    log("event: $event")
    it.drop(1).apply {
      val (trigger, update, settle, token) = key.split(";")
      when (event) {
        "INVITE" -> {
          events += ChannelInviteReceived(
            ChannelInvite(keys = Channel.Keys(trigger, update, settle), tokenId = token, balance = this[0].toBigDecimal(), address = this[1], maximaPK = senderPK),
            transport = MAXIMA
          )
          view = "Channel Events"
        }
        "ACCEPTED" -> scope.launch {
          requestChannelAccepted(this@apply, checkNotNull(inviteSent), senderPK)
        }
        "CONFIRMED" -> scope.launch {
          fundChannelConfirmed(this@apply, checkNotNull(channelToFund))
        }
        else -> scope.launch {
          storage.getChannel(Channel.Keys(trigger, update, settle))?.let { channel ->
            channelService.processMessage(payload, { _, _ -> }, {}, { channel.id }) {
              view = "Channel Events"
            }
          } ?: log("Channel not found for keys: $key")
        }
      }
    }
  }
}