package ltd.mbor.minipay.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.delete
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.ui.preview.fakeChannel
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun DeleteChannel(channel: Channel, onDelete: (Channel) -> Unit) {
  var deleteDisabled by remember { mutableStateOf(false) }
  var confirmDialogOpen by remember { mutableStateOf(false) }
  Button(enabled = !deleteDisabled, onClick = {
    scope.launch{
      deleteDisabled = true
      confirmDialogOpen = true
    }
  }) {
    Text("\uD83D\uDDD1 Delete")
  }

  if (confirmDialogOpen) {
    AlertDialog(
      onDismissRequest = {
        confirmDialogOpen = false
        deleteDisabled = false
      },
      title = {
        Text(text = "Confirm channel deletion")
      },
      text = {
        Text("Delete ${channel.status} channel ${channel.id}?")
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              confirmDialogOpen = false
              channel.delete()
              onDelete(channel)
            }
          }) {
          Text("Delete")
        }
      },
      dismissButton = {
        Button(
          onClick = {
            confirmDialogOpen = false
            deleteDisabled = false
          }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable @Preview
fun PreviewDeleteChannel() {
  MiniPayTheme {
    DeleteChannel(fakeChannel) {}
  }
}