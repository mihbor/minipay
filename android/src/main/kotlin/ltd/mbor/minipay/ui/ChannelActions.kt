package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.blockNumber
import ltd.mbor.minipay.common.Channel

@Composable
fun ChannelActions(
  channel: Channel,
  eltooScriptCoins: Map<String, List<Coin>>,
  activity: MainActivity?,
  updateChannel: (Channel) -> Unit
) {
  Row {
    Column(Modifier.width(350.dp)) {
      if (channel.status == "OPEN") {
        ChannelTransfers(channel, activity)
      }
      Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
      if (channel.status in setOf("OFFERED", "SETTLED")) {
        DeleteChannel(channel, updateChannel)
      }
    }
  }
}