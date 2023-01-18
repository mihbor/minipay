package ui

import androidx.compose.runtime.Composable
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minipay.common.Channel
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelActions(
  channel: Channel,
  updateChannel: (Channel) -> Unit
) {
  if (channel.status == "OPEN") {
    ChannelTransfers(channel)
  }
  Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
  Br()
  Button({
    disabled()
  }) {
    Text("\uD83D\uDDD1 Delete")
  }
}