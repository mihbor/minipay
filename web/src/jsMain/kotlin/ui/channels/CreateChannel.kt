package ui.channels

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.newKeys
import org.jetbrains.compose.web.css.LineStyle.Companion.Inset
import org.jetbrains.compose.web.css.LineStyle.Companion.Outset
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.dom.*
import ui.CopyToClipboard

@Composable
fun CreateChannel(
  balances: SnapshotStateMap<String, Balance>,
  tokens: SnapshotStateMap<String, Token>,
) {
  var isInviting by remember { mutableStateOf(true) }
  var useMaxima by remember { mutableStateOf(false) }
  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var myAddress by remember { mutableStateOf("") }
  var maximaContact by remember { mutableStateOf<Contact?>(null) }

  LaunchedEffect("createChannel") {
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
    onClick { useMaxima = !useMaxima }
    style {
      border(style = if (useMaxima) Outset else Inset)
    }
  }){
    Text("Firebase")
  }
  Button({
    onClick { useMaxima = !useMaxima }
    style {
      border(style = if (useMaxima) Inset else Outset)
    }
  }){
    Text("Maxima")
  }
  if (useMaxima) {
    val contacts = remember { mutableStateListOf<Contact>() }
    LaunchedEffect("contacts") {
      contacts.addAll(MDS.getContacts())
    }
    Select({
      onChange {
        maximaContact = it.value?.toIntOrNull()?.let { contactId -> contacts.find { it.id == contactId } }
      }
    }) {
      Option("") {
        Text("Please select:")
      }
      contacts.forEach { contact ->
        Option(contact.id.toString()) {
          Text(contact.extraData.name)
        }
      }
    }
  }
  Br()
  Button({
    onClick { isInviting = !isInviting }
    style {
      border(style = if (isInviting) Inset else Outset)
    }
  }){
    Text("Invite")
  }
  Button({
    onClick { isInviting = !isInviting }
    style {
      border(style = if (isInviting) Outset else Inset)
    }
  }){
    Text("Join")
  }
  Br()
  if (isInviting) RequestChannel(myKeys, myAddress, balances, tokens, useMaxima, if(useMaxima) maximaContact else null)
  else FundChannel(myKeys, myAddress, balances, tokens)
}