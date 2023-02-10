package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.preview.fakeChannelTriggered
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelActions(
  channel: Channel,
  activity: MainActivity?,
  setChannel: (Channel) -> Unit,
  updateChannel: (Channel) -> Unit
) {
  Column(Modifier.fillMaxWidth()) {
    if (channel.status == "OPEN") {
      ChannelTransfers(channel, activity)
    }
    Row {
      if (channel.status in setOf("OFFERED", "SETTLED")) {
        DeleteChannel(channel, updateChannel)
      }
      Spacer(Modifier.weight(1f))
      Button({
        setChannel(channel)
      }) {
        Text("Details")
      }
    }
  }
}

@Composable @Preview
fun PreviewOpenChannelActions() {
  MiniPayTheme {
    ChannelActions(fakeChannelOpen, null, {}) {}
  }
}

@Composable @Preview
fun PreviewTriggeredChannelActions() {
  MiniPayTheme {
    ChannelActions(fakeChannelTriggered, null, {}) {}
  }
}
