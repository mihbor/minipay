package logic

import androidx.compose.runtime.*
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.common.channelService
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.processUpdate

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
  return processUpdate(isAck, updateTxText, settleTxText)  {
    channels[channels.indexOf(channels.first{ it.id == id })] = it
    if (isAck) events.removeIf { it.channel.id == id && it is PaymentRequestSent }
  }
}

fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
  removeAll(filter(predicate))
}

suspend fun MutableList<Channel>.reload(eltooScriptCoins: MutableMap<String, List<Coin>>) {
  channelService.reloadChannels(this, eltooScriptCoins)
}