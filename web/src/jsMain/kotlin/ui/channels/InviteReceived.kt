package ui.channels

import androidx.compose.runtime.Composable
import channelInvite
import ltd.mbor.minipay.common.model.ChannelInvite
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import view

@Composable
fun InviteReceived(invite: ChannelInvite, dismiss: () -> Unit) {
  Text("Invite received to join payment channel")
  Button({
    onClick {
      dismiss()
    }
  }) {
    Text("Dismiss")
  }
  Button({
    onClick {
      channelInvite = invite
      view = "Create Channel"
    }
  }) {
    Text("Review")
  }
}