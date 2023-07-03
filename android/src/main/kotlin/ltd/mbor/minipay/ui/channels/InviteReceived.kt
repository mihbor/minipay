package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ltd.mbor.minipay.channelInvite
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.logic.channelToFund
import ltd.mbor.minipay.logic.requestedChannel
import ltd.mbor.minipay.view

@Composable
fun InviteReceived(invite: ChannelInvite, dismiss: () -> Unit) {

  Column {
    Text("Request received to join payment channel")
    Button(onClick = {
      dismiss()
    }) {
      Text("Dismiss")
    }
    Button(onClick = {
      channelInvite = invite
      requestedChannel = null
      channelToFund = null
      view = "Create Channel"
    }) {
      Text("Review")
    }
  }
}