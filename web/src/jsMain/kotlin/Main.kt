import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import logic.*
import ltd.mbor.minipay.common.Prefs
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposableInBody
import ui.*

val scope = MainScope()
external fun require(module: String): dynamic

var view by mutableStateOf("MiniPay")

fun main() {
  var prefs by mutableStateOf(Prefs(getParams("uid") ?: "", window.location.hostname, 9004))
  initFirebase()
  initMDS(prefs)
  renderComposableInBody {
    Style(StyleSheets)
    Menu(view) { view = it }
    when(view) {
      "MiniPay" -> Welcome { view = it }
      "Receive" -> Receive(balances, tokens)
      "Send" -> Send(balances)
      "Fund channel" -> FundChannel(balances, tokens)
      "Request channel" -> RequestChannel(balances, tokens)
      "Channels" -> ChannelListing(channels, eltooScriptCoins)
      "Channel events" -> ChannelEvents(events, tokens)
      "Settings" -> Settings(prefs) {
        prefs = it
        initMDS(prefs)
      }
    }
  }
}