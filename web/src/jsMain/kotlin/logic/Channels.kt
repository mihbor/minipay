package logic

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.log
import ltd.mbor.minipay.common.channelService
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.processRequest
import ltd.mbor.minipay.common.processUpdate
import ltd.mbor.minipay.common.subscribe
import scope
import view

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
  removeAll(filter(predicate))
}

suspend fun MutableList<Channel>.reload(eltooScriptCoins: MutableMap<String, List<Coin>>) {
  channelService.reloadChannels(this, eltooScriptCoins)
}

suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
  return processUpdate(isAck, updateTxText, settleTxText)  {
    channels[channels.indexOf(channels.first{ it.id == id })] = it
    if (isAck) events.removeIf { it.channel.id == id && it is PaymentRequestSent }
  }
}

fun String.subscribe(
  initialChannel: Channel?,
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: (suspend (List<String>) -> Channel)? = null
) {
  var channel = initialChannel
  subscribe(this).onEach { msg ->
    log("tx msg: $msg")
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channel = channel!!.processUpdate(isAck, updateTxText = splits[1], settleTxText = splits[2]).also {
        onUpdate(it, isAck)
      }
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      channel!!.processRequest(updateTxText, settleTxText) {
        events += it
        view = "Channel events"
      }
    } else if (onUnhandled != null) {
      channel = onUnhandled(splits)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}
