package ui.channels

import androidx.compose.runtime.Composable
import channelInvite
import logic.channelToFund
import logic.requestedChannel
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
      requestedChannel = null
      channelToFund = null
      view = "Create Channel"
    }
  }) {
    Text("Review")
  }
}