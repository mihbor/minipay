package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelDetails(
  channel: Channel,
  balances: Map<String, Balance>,
  activity: MainActivity?,
  updateChannel: (Channel?) -> Unit
) {
  Column {
    Row(Modifier.fillMaxWidth()) {
      Button({ updateChannel(null) }) {
        Text("< Channel listing")
      }
    }
    ChannelView(channel, balances, activity, updateChannel)
  }
}

@Composable @Preview
fun PreviewChannelDetails() {
  MiniPayTheme {
    ChannelDetails(fakeChannelOpen, fakeBalances, null, {})
  }
}