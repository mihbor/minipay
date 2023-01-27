package ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.delete
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope

@Composable
fun DeleteChannel(channel: Channel, onDelete: (Channel) -> Unit) {
  var disabled by remember { mutableStateOf(false) }
  Button({
    if (disabled) disabled()
    onClick {
      if (window.confirm("Delete ${channel.status} channel ${channel.id}?")) scope.launch {
        disabled = true
        channel.delete()
        onDelete(channel)
      }
    }
  }) {
    Text("\uD83D\uDDD1 Delete")
  }
}