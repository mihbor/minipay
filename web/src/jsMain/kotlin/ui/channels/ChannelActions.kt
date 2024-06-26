package ui.channels

import androidx.compose.runtime.Composable
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.common.model.Channel
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelActions(
  channel: Channel,
  balances: Map<String, Balance>,
  selectChannel: (Channel) -> Unit,
  updateChannel: (Channel?) -> Unit
) {
  if (channel.status == "OPEN") {
    ChannelTransfers(channel, balances)
  }
  Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
  Br()
  if (channel.status in setOf("OFFERED", "SETTLED")) {
    DeleteChannel(channel, updateChannel)
  }
  Button({
    onClick { selectChannel(channel) }
  }) {
    Text("Details")
  }
}