package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelRequestSent(dismiss: () -> Unit) {
  Text("Waiting for counterparty to accept request")
  Button({
    onClick {
      dismiss()
    }
  }) {
    Text("Cancel")
  }
}