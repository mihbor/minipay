package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.blockNumber
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.delete
import ltd.mbor.minipay.scope

@Composable
fun ChannelActions(
  channel: Channel,
  eltooScriptCoins: Map<String, List<Coin>>,
  activity: MainActivity?,
  updateChannel: (Channel) -> Unit
) {
  var disabled by remember { mutableStateOf(false) }
  Row {
    Column(Modifier.width(350.dp)) {
      if (channel.status == "OPEN") {
        ChannelTransfers(channel, activity)
      }
      Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
      if (channel.status in setOf("OFFERED", "SETTLED")) {
        Button(enabled = !disabled, onClick = {
          scope.launch{
            disabled = true
            channel.delete()
            updateChannel(channel)
          }
        }) {
          Text("\uD83D\uDDD1 Delete")
        }
      }
    }
  }
}