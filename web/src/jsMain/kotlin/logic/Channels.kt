package logic

import androidx.compose.runtime.*
import com.benasher44.uuid.Uuid
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.common.ChannelService
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import view

val channels = mutableStateMapOf<Uuid, Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

lateinit var channelService: ChannelService

fun Channel.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Unit = { }
) {
  with(channelService) {
    channelKey(my.keys, tokenId).subscribe({ id }, onUpdate, onUnhandled) {
      view = "Channel Events"
    }
  }
}

fun String.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Uuid?
) {
  var channelId: Uuid? = null
  with(channelService) {
    subscribe({ checkNotNull(channelId) }, onUpdate, {
      onUnhandled(it)?.let{ channelId = it }
    }, {
      view = "Channel Events"
    })
  }
}
