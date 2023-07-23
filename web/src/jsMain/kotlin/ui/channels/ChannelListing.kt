package ui.channels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.launch
import logic.channelService
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.*
import ui.TokenIcon

@Composable
fun ChannelListing(
  channels: MutableMap<Uuid, Channel>,
  balances: Map<String, Balance>,
  eltooScriptCoins: MutableMap<String, List<Coin>>,
  selectChannel: (Channel?) -> Unit,
) {
  LaunchedEffect("channels") {
    channelService.reloadChannels(eltooScriptCoins)
  }
  Button({
    onClick {
      scope.launch {
        channelService.reloadChannels(eltooScriptCoins)
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
        Th { Text("Name") }
        Th { Text("Status") }
        Th { Text("Sequence number") }
        Th { Text("Token") }
        Th { Text("My balance") }
        Th { Text("Their balance") }
        Th { Text("Actions") }
      }
    }
    Tbody {
      channels.values.sortedBy{ it.name }.forEach { channel ->
        key(channel.id) {
          Tr({
            style { property("border-top", "1px solid black") }
          }) {
            Td { Text(channel.name) }
            Td { Text(channel.status) }
            Td { Text(channel.sequenceNumber.toString()) }
            Td {
              TokenIcon(channel.tokenId, balances)
              Text(balances[channel.tokenId]?.tokenName ?: if (channel.tokenId == "0x00") "Minima" else "[${channel.tokenId.take(8)}...]")
            }
            Td { Text(channel.my.balance.toPlainString()) }
            Td { Text(channel.their.balance.toPlainString()) }
            Td({
              style {
                textAlign("left")
              }
            }) {
              ChannelActions(channel, balances, selectChannel) {
                scope.launch { channelService.reloadChannels(eltooScriptCoins) }
              }
            }
          }
        }
      }
    }
  }
}
