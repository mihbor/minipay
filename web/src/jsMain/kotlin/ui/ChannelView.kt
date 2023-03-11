package ui

import androidx.compose.runtime.Composable
import logic.blockNumber
import logic.eltooScriptCoins
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.common.model.Channel
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelView(
  channel: Channel,
  balances: Map<String, Balance>,
  updateChannel: (Channel) -> Unit
) {
  Div({
    style { margin(10.px) }
  }) {
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