package ui

import androidx.compose.runtime.Composable
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minipay.common.Channel
import org.jetbrains.compose.web.dom.Br

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
  if (channel.status in setOf("OFFERED", "SETTLED")) {
    DeleteChannel(channel, updateChannel)
  }
}