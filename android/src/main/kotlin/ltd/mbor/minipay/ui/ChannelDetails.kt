package ltd.mbor.minipay.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannel
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelDetails(
  channel: Channel,
  balances: Map<String, Balance>,
  activity: MainActivity?,
  updateChannel: (Channel) -> Unit
) {
  var showDetails by remember { mutableStateOf(false) }
  Button({
    showDetails = !showDetails
  }) {
    Text("Details", fontSize = 10.sp)
  }
  if (showDetails) AlertDialog(
    onDismissRequest = { showDetails = false },
    title = {
      Text("Details of channel ${channel.id}")
    },
    text = {
      ChannelView(channel, balances, activity, updateChannel)
    },
    confirmButton = {
      Button({ showDetails = false }) {
        Text("Back", fontSize = 10.sp)
      }
    }
  )
}

@Composable @Preview
fun PreviewChannelDetails() {
  MiniPayTheme {
    ChannelDetails(fakeChannel, fakeBalances, null) {}
  }
}