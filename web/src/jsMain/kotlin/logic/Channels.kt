package logic

import androidx.compose.runtime.*
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MDS
import ltd.mbor.minipay.common.ChannelService
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.storage
import view

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

val channelService = ChannelService(MDS, storage, channels, events)

fun Channel.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Unit = { }
) {
  with(channelService) {
    channelKey(my.keys, tokenId).subscribe({ id }, onUpdate, onUnhandled) {
      view = "Channel events"
    }
  }
}

fun String.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Int
) {
  var channelId: Int? = null
  with(channelService) {
    subscribe({ checkNotNull(channelId) }, onUpdate, {
      channelId = onUnhandled(it)
    }, {
      view = "Channel events"
    })
  }
}
