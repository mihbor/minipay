package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import logic.balances
import logic.reload
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.common.Channel
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.*
import scope

@Composable
fun ChannelListing(
  channels: MutableList<Channel>,
  eltooScriptCoins: MutableMap<String, List<Coin>>,
  selectChannel: (Channel?) -> Unit,
) {
  LaunchedEffect("channels") {
    channels.reload(eltooScriptCoins)
  }
  Button({
    onClick {
      scope.launch {
        channels.reload(eltooScriptCoins)
      }
    }
  }) {
    Text("Refresh")
  }
  Table({
    style {
      textAlign("right")
      property("border-collapse", "collapse")
    }
  }) {
    Thead {
      Tr {
        Th { Text("ID") }
        Th { Text("Status") }
        Th { Text("Sequence number") }
        Th { Text("Token") }
        Th { Text("My balance") }
        Th { Text("Their balance") }
        Th { Text("Actions") }
      }
    }
    Tbody {
      channels.forEach { channel ->
        Tr({
          style { property("border-top", "1px solid black") }
        })  {
          Td{ Text(channel.id.toString()) }
          Td{ Text(channel.status) }
          Td{ Text(channel.sequenceNumber.toString()) }
          Td{
            TokenIcon(channel.tokenId, balances)
            Text(balances[channel.tokenId]?.tokenName ?: if(channel.tokenId == "0x00") "Minima" else "[${channel.tokenId.take(8)}...]")
          }
          Td{ Text(channel.my.balance.toPlainString()) }
          Td{ Text(channel.their.balance.toPlainString()) }
          Td({
            style {
              textAlign("left")
            }
          }) {
            ChannelActions(channel, selectChannel) {
              scope.launch { channels.reload(eltooScriptCoins) }
            }
          }
        }
      }
    }
  }
}
