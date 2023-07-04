import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.*
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minipay.common.ChannelService
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.common.storage
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.get
import org.w3c.dom.set
import ui.*
import ui.channels.ChannelDetails
import ui.channels.ChannelEvents
import ui.channels.ChannelListing
import ui.channels.CreateChannel

external fun require(module: String): dynamic

var view by mutableStateOf("MiniPay")
var channelInvite by mutableStateOf(ChannelInvite.EMPTY)
var maximaContact by mutableStateOf<Contact?>(null)

fun main() {
  var prefs by mutableStateOf(Prefs(
    uid = getParams("uid") ?: localStorage["uid"] ?: "",
    host = localStorage["host"]?.takeUnless { it.isBlank() } ?: window.location.hostname,
    port = localStorage["port"]?.toIntOrNull() ?: ((window.location.port.toIntOrNull() ?: 9003) + 1)
  ))
  scope.launch {
    initMDS(prefs)
  }
  channelService = ChannelService(MDS, storage, initFirebase(), channels, events)

  var channel by mutableStateOf<Channel?>(null)
  fun selectChannel(newChannel: Channel?) {
    channel = newChannel
    view = if (newChannel != null) "Channel Details" else "Channels"
  }

  renderComposableInBody {
    Style(StyleSheets)
    Menu(view) { view = it }
    when(view) {
      "MiniPay" -> Welcome { view = it }
      "Receive" -> Receive(balances, tokens)
      "Send" -> Send(balances)
      "Create Channel" -> CreateChannel(channels, balances, tokens, channelInvite, { channelInvite = it }, maximaContact) { maximaContact = it }
      "Channels" -> ChannelListing(channels, balances, eltooScriptCoins, ::selectChannel)
      "Channel Events" -> ChannelEvents(events, tokens)
      "Channel Details" -> channel?.let{
        ChannelDetails(it, balances, ::selectChannel) {}
      }
      "Contacts" -> Contacts {
        maximaContact = it
        view = "Create Channel"
      }
      "Settings" -> Settings(prefs) {
        prefs = it
        scope.launch {
          initMDS(prefs)
          if (inited) {
            localStorage["uid"] = prefs.uid
            localStorage["host"] = prefs.host
            localStorage["port"] = prefs.port.toString()
          }
        }
      }
    }
  }
}