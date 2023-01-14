import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import logic.*
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposableInBody
import ui.*

val scope = MainScope()
external fun require(module: String): dynamic

fun main() {
  scope.launch {
    initFirebase()
    init(getParams("uid"))
  }
  renderComposableInBody {
    Style(StyleSheets)
    var view by remember { mutableStateOf("Settings") }
    requestReceivedOnChannel?.let {
      ChannelRequestReceived(it, updateTx!!, settleTx!!) {
        requestReceivedOnChannel = null
        updateTx = null
        settleTx = null
      }
    }
    requestSentOnChannel?.let {
      ChannelRequestSent{ requestSentOnChannel = null }
    }
    if (requestReceivedOnChannel == null && requestSentOnChannel == null) {
      Menu(view) { view = it }
      when(view) {
        "Receive" -> Receive(balances, tokens)
        "Send" -> Send(balances)
        "Fund channel" -> FundChannel(balances, tokens) { requestSentOnChannel = it }
        "Request channel" -> RequestChannel(balances, tokens) { requestSentOnChannel = it }
        "Channels" -> ChannelListing(channels) { requestSentOnChannel = it }
        "Settings" -> Settings()
      }
    }
  }
}