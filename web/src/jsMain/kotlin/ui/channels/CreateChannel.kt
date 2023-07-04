package ui.channels

import androidx.compose.runtime.*
import com.benasher44.uuid.Uuid
import logic.channelToFund
import logic.requestedChannel
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.ChannelInvite.Companion.EMPTY
import ltd.mbor.minipay.common.newKeys
import org.jetbrains.compose.web.css.LineStyle.Companion.Inset
import org.jetbrains.compose.web.css.LineStyle.Companion.Outset
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import ui.ContactSelect
import ui.CopyToClipboard

@Composable
fun CreateChannel(
  channels: Map<Uuid, Channel>,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  invite: ChannelInvite,
  setInvite: (ChannelInvite) -> Unit,
  maximaContact: Contact?,
  selectContact: (Contact?) -> Unit
) {
  var isInviting by remember { mutableStateOf(invite == EMPTY) }
  var useMaxima by remember { mutableStateOf(maximaContact != null || invite != EMPTY) }
  var myKeys by remember { mutableStateOf(Channel.Keys.EMPTY) }
  var myAddress by remember { mutableStateOf("") }

  LaunchedEffect("createChannel") {
    (channels[channelToFund?.id] ?: channelToFund)?.takeIf { it.status == "OPEN" }?.let {
      log("channelToFund is open")
      channelToFund = null
    }
    (channels[requestedChannel?.id] ?: requestedChannel)?.takeIf { it.status == "OPEN" }?.let {
      log("requestedChannel is open")
      requestedChannel = null
    }
    MDS.newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    myAddress = MDS.getAddress().address
  }

  Text("Trigger key: ${myKeys.trigger}")
  CopyToClipboard(myKeys.trigger)
  Br()
  Text("Update key: ${myKeys.update}")
  CopyToClipboard(myKeys.update)
  Br()
  Text("Settlement key: ${myKeys.settle}")
  CopyToClipboard(myKeys.settle)
  Br()
  Text("Address: $myAddress")
  CopyToClipboard(myAddress)
  Br()
  Br()
  Text("Transport:")
  Button({
    onClick { useMaxima = false }
    style {
      border(style = if (useMaxima) Outset else Inset)
    }
  }){
    Text("Firebase")
  }
  Button({
    onClick { useMaxima = true }
    style {
      border(style = if (useMaxima) Inset else Outset)
    }
  }){
    Text("Maxima")
  }
  if (useMaxima && isInviting) ContactSelect(maximaContact, selectContact)
  Br()
  Button({
    onClick { isInviting = true }
    style {
      border(style = if (isInviting) Inset else Outset)
    }
  }){
    Text("Invite")
  }
  Button({
    onClick { isInviting = false }
    style {
      border(style = if (isInviting) Outset else Inset)
    }
  }){
    Text("Join")
  }
  Br()
  if (isInviting) RequestChannel(myKeys, myAddress, balances, tokens, useMaxima, if(useMaxima) maximaContact else null)
  else FundChannel(myKeys, myAddress, balances, tokens, invite, setInvite)
}