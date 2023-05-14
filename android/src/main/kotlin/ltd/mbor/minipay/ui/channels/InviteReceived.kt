package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ltd.mbor.minipay.view

@Composable
fun InviteReceived(dismiss: () -> Unit) {

  Column {
    Text("Request received to join payment channel")
    Button(onClick = {
      dismiss()
    }) {
      Text("Reject")
    }
    Button(onClick = {
      view = "Create Channel"
    }) {
      Text("Review")
    }
  }
}