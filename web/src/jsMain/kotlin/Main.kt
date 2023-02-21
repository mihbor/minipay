import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import logic.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.Prefs
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposableInBody
import ui.*

external fun require(module: String): dynamic

var view by mutableStateOf("MiniPay")

fun main() {
  var prefs by mutableStateOf(Prefs(getParams("uid") ?: "", window.location.hostname, 9004))
  initMDS(prefs)
  initFirebase()

  var channel by mutableStateOf<Channel?>(null)
  fun selectChannel(newChannel: Channel?) {
    channel = newChannel
    view = if (newChannel != null) "Channel details" else "Channels"
  }

  renderComposableInBody {
    Style(StyleSheets)
    Menu(view) { view = it }
    when(view) {
      "MiniPay" -> Welcome { view = it }
      "Receive" -> Receive(balances, tokens)
      "Send" -> Send(balances)
      "Fund channel" -> FundChannel(balances, tokens)
      "Request channel" -> RequestChannel(balances, tokens)
      "Channels" -> ChannelListing(channels, eltooScriptCoins, ::selectChannel)
      "Channel events" -> ChannelEvents(events, tokens)
      "Channel details" -> channel?.let{
        ChannelDetails(it, balances, ::selectChannel) {}
      }
      "Settings" -> Settings(prefs) {
        prefs = it
        initMDS(prefs)
      }
    }
  }
}