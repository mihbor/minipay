package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.blockNumber
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.rename
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.eltooScriptCoins
import ltd.mbor.minipay.ui.CopyToClipboard
import ltd.mbor.minipay.ui.DeleteChannel
import ltd.mbor.minipay.ui.Settlement
import ltd.mbor.minipay.ui.TokenIcon
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.preview.fakeChannelTriggered

@Composable
fun ChannelView(
  channel: Channel,
  balances: Map<String, Balance>,
  activity: MainActivity?,
  updateChannel: (Channel) -> Unit
) {
  var channelName by remember { mutableStateOf(channel.name) }
  fun renameChannel() {
    scope.launch {
      updateChannel(channel.rename(channelName))
    }
  }
  Column{
    Row{
      Text("Id: ")
      Text(channel.id.toString())
    }
    Divider()
    Row{
      OutlinedTextField(
        channelName,
        { channelName = it },
        Modifier.height(50.dp),
        textStyle = TextStyle(fontSize = 12.sp)
      )
      Button(
        ::renameChannel
      ) {
        Text("Re-\nname")
      }
    }
    Divider()
    Row{
      Text("Token: ")
      TokenIcon(channel.tokenId, balances)
      Text(balances[channel.tokenId]?.tokenName ?: "[${channel.tokenId}]")
    }
    Divider()
    Row {
      Text("My balance: ")
      Text(channel.my.balance.toPlainString(), Modifier.width(60.dp))
      Text("Their balance: ")
      Text(channel.their.balance.toPlainString(), Modifier.width(60.dp))
    }
    Divider()
    Row {
      Text("Status: ${channel.status}")
    }
    Divider()
    Row {
      Text("Sequence number: ${channel.sequenceNumber}")
    }
    Divider()
    Row {
      Text("Multi-signature script address:")
    }
    Row {
      Text(channel.multiSigAddress, Modifier.fillMaxWidth(0.8f))
      CopyToClipboard(channel.multiSigAddress)
    }
    Divider()
    Row {
      Text("Eltoo script address:")
    }
    Row {
      Text(channel.eltooAddress, Modifier.fillMaxWidth(0.8f))
      CopyToClipboard(channel.eltooAddress)
    }
    Divider()
    if (channel.status == "OPEN") {
      ChannelTransfers(channel, balances, activity)
      Divider()
    }
    Row(Modifier.fillMaxWidth()) {
      Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
      if (channel.status in setOf("OFFERED", "SETTLED")) {
        Spacer(Modifier.weight(1f))
        DeleteChannel(channel, updateChannel)
      }
    }
  }
}

@Composable @Preview
fun PreviewChannelViewOpen() {
  ChannelView(channel = fakeChannelOpen, balances = fakeBalances, activity = null, updateChannel = {})
}

@Composable @Preview
fun PreviewChannelViewTriggered() {
  ChannelView(channel = fakeChannelTriggered, balances = fakeBalances, activity = null, updateChannel = {})
}