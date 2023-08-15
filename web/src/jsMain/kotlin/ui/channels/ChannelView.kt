package ui.channels

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.rename
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import ui.CopyToClipboard
import ui.TokenIcon

@Composable
fun ChannelView(
  channel: Channel,
  balances: Map<String, Balance>,
  updateChannel: (Channel?) -> Unit
) {
  var channelName by remember { mutableStateOf(channel.name) }
  Div({
    style { margin(10.px) }
  }) {
    Div {
      Text("ID: ${channel.id}")
    }
    Div {
      Text("Name:")
      TextInput(channelName) {
        onInput {
          channelName = it.value
        }
      }
      Button({
        onClick {
          scope.launch {
            updateChannel(channel.rename(channelName))
          }
        }
      }) {
        Text("Rename")
      }
    }
    Div {
      Text("Token: ")
      TokenIcon(channel.tokenId, balances)
    }
    Div {
      Span({
        style { marginRight(50.px) }
      }) {
        Text("My balance: ")
        Text(channel.my.balance.toPlainString())
      }
      Text("Their balance: ")
      Text(channel.their.balance.toPlainString())
    }
    Div {
      Text("Status: ${channel.status}")
    }
    Div {
      Text("Sequence number: ${channel.sequenceNumber}")
    }
    Div {
      Text("Multi-signature script address:")
      Br()
      Text(channel.multiSigAddress)
      CopyToClipboard(channel.multiSigAddress)
    }
    Div {
      Text("Eltoo script address:")
      Br()
      Text(channel.eltooAddress)
      CopyToClipboard(channel.eltooAddress)
    }
    if (channel.status == "OPEN") {
      ChannelTransfers(channel, balances)
    }
    Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
    if (channel.status in setOf("OFFERED", "SETTLED")) {
      DeleteChannel(channel, updateChannel)
    }
  }
}