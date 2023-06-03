package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.decodeHex
import ltd.mbor.minimak.jsonString
import ltd.mbor.minimak.log
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.ChannelInviteReceived
import ltd.mbor.minipay.common.model.Transport.MAXIMA
import ltd.mbor.minipay.view

fun onMessage(msg: JsonElement) {
  val data = msg.jsonString("data").substring(2).decodeHex().decodeToString()
  val senderPK = msg.jsonString("from")
  log("received: $data from $senderPK")
  val (command, payload) = data.split(":")
  payload.split(";").apply {
    when (command) {
      "invite" -> {
        events += ChannelInviteReceived(
          ChannelInvite(keys = Channel.Keys(this[0], this[1], this[2]), tokenId = this[3], balance = this[4].toBigDecimal(), address = this[5], maximaPK = senderPK),
          transport = MAXIMA
        )
        view = "Channel Events"
      }
    }
  }
}