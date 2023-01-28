package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelRequestSent(dismiss: () -> Unit) {
  Column {
    Text("Waiting for counterparty to accept request")
    Button(onClick = {
      dismiss()
    }) {
      Text("Cancel")
    }
  }
}

@Composable @Preview
fun PreviewChannelRequestSent() {
  MiniPayTheme {
    ChannelRequestSent {}
  }
}