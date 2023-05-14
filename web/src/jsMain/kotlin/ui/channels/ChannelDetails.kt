package ui.channels

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.common.model.Channel
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelDetails(
  channel: Channel,
  balances: Map<String, Balance>,
  selectChannel: (Channel?) -> Unit,
  updateChannel: (Channel?) -> Unit
) {
  Div {
    Button({
      onClick { selectChannel(null) }
    }) {
      Text("< Channel listing")
    }
    ChannelView(channel, balances, updateChannel)
  }
}