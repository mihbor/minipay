package ltd.mbor.minipay.common.transport

import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.decodeHex
import ltd.mbor.minimak.jsonString
import ltd.mbor.minimak.log

fun onMessage(msg: JsonElement) {
  val data = msg.jsonString("data").substring(2).decodeHex().decodeToString()
  val sender = msg.jsonString("from")
  log("received: $data from $sender")
  val (command, payload) = data.split(":")
  val splits = payload.split(";")
  when(command) {
    "invite" -> {

    }
  }
}