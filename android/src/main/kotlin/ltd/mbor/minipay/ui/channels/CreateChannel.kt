package ltd.mbor.minipay.ui.channels

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.benasher44.uuid.Uuid
import ltd.mbor.minimak.*
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.ChannelInvite.Companion.EMPTY
import ltd.mbor.minipay.common.newKeys
import ltd.mbor.minipay.logic.channelToFund
import ltd.mbor.minipay.logic.requestedChannel
import ltd.mbor.minipay.ui.ContactSelect
import ltd.mbor.minipay.ui.CopyToClipboard
import ltd.mbor.minipay.ui.preview.*
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun CreateChannel(
  channels: Map<Uuid, Channel>,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  activity: MainActivity?,
  invite: ChannelInvite,
  setInvite: (ChannelInvite) -> Unit,
  maximaContact: Contact?,
  selectContact: (Contact?) -> Unit
) {

  var isInviting by remember { mutableStateOf(invite == EMPTY) }
  var useMaxima by remember { mutableStateOf(maximaContact != null || invite != EMPTY) }
  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var myAddress by remember { mutableStateOf("") }
  val context = LocalContext.current

  LaunchedEffect("createChannel") {
    try {
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
    } catch (e: MinimaException) {
      Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
    }
  }

  LazyColumn {
    item {
      ProvideTextStyle(value = TextStyle(fontSize = 12.sp)) {
        Row {
          Text("Trigger key: ${myKeys.trigger}", Modifier.fillMaxWidth(0.8f))
          CopyToClipboard(myKeys.trigger)
        }
        Row {
          Text("Update key: ${myKeys.update}", Modifier.fillMaxWidth(0.8f))
          CopyToClipboard(myKeys.update)
        }
        Row {
          Text("Settlement key: ${myKeys.settle}", Modifier.fillMaxWidth(0.8f))
          CopyToClipboard(myKeys.settle)
        }
        Row {
          Text("Address: $myAddress", Modifier.fillMaxWidth(0.8f))
          CopyToClipboard(myAddress)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Transport:  ")
          Text("Firebase")
          Switch(checked = useMaxima, onCheckedChange = { useMaxima = it })
          Text("Maxima")
        }
        if (useMaxima && isInviting) ContactSelect(maximaContact, selectContact)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Invite")
          Switch(checked = !isInviting, onCheckedChange = { isInviting = !it })
          Text("Join")
        }
        if (isInviting) RequestChannel(myKeys, myAddress, balances, tokens, if(useMaxima) maximaContact else null, activity)
        else FundChannel(myKeys, myAddress, balances, tokens, activity, invite, setInvite)
      }
    }
  }
}

@Composable @Preview
fun PreviewCreateChannel() {
  MiniPayTheme {
    CreateChannel(previewChannels, previewBalances, previewTokens, null, EMPTY, {}, null, {})
  }
}

@Composable @Preview
fun PreviewCreateChannelWithInvite() {
  MiniPayTheme {
    CreateChannel(previewChannels, previewBalances, previewTokens, null, previewInvite, {}, null, {})
  }
}

@Composable @Preview
fun PreviewCreateChannelWithContact() {
  MiniPayTheme {
    CreateChannel(previewChannels, previewBalances, previewTokens, null, EMPTY, {}, alice, {})
  }
}
