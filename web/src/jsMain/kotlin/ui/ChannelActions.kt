package ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.delete
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope

@Composable
fun ChannelActions(
  channel: Channel,
  updateChannel: (Channel) -> Unit
) {
  var disabled by remember { mutableStateOf(false) }
  if (channel.status == "OPEN") {
    ChannelTransfers(channel)
  }
  Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
  Br()
  if (channel.status in setOf("OFFERED", "SETTLED")) {
    Button({
      if (disabled) disabled()
      onClick {
        if (window.confirm("Delete ${channel.status} channel ${channel.id}?")) scope.launch {
          disabled = true
          channel.delete()
          updateChannel(channel)
        }
      }
    }) {
      Text("\uD83D\uDDD1 Delete")
    }
  }
}