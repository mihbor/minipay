package ui.channels

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import view

@Composable
fun InviteReceived(
  dismiss: () -> Unit
) {
  Text("Invite received to join payment channel")
  Button({
    onClick {
      dismiss()
    }
  }) {
    Text("Reject")
  }
  Button({
    onClick {
      view = "Create Channel"
    }
  }) {
    Text("Review")
  }
}