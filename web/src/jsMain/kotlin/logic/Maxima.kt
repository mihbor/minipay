package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.decodeHex
import ltd.mbor.minimak.jsonString
import ltd.mbor.minimak.log
import ltd.mbor.minipay.common.model.ChannelInviteReceived
import ltd.mbor.minipay.common.model.Transport.MAXIMA
import view

fun onMessage(msg: JsonElement) {
  val data = msg.jsonString("data").substring(2).decodeHex().decodeToString()
  val sender = msg.jsonString("from")
  log("received: $data from $sender")
  val (command, payload) = data.split(":")
  val splits = payload.split(";")
  val theirBalance = splits[4].toBigDecimal()
  when(command) {
    "invite" -> {
      events += ChannelInviteReceived(ZERO to theirBalance, MAXIMA)
      view = "Channel Events"
    }
  }
}