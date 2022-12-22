package ui

import Channel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import logic.*
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getCoins
import org.jetbrains.compose.web.dom.*
import scope

@Composable
fun ChannelListing(channels: MutableList<Channel>) {
  LaunchedEffect("channels") {
    channels.load()
  }
  Button({
    onClick {
      scope.launch {
        channels.load()
      }
    }
  }) {
    Text("Refresh")
  }
  Table {
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
      channels.forEachIndexed { index, channel ->
        Tr {
          Td { Text(channel.id.toString()) }
          Td { Text(channel.status) }
          Td { Text(channel.sequenceNumber.toString()) }
          Td {
            TokenIcon(channel.tokenId, balances)
            Text(balances[channel.tokenId]?.tokenName ?: "[${channel.tokenId}]")
          }
          Td { Text(channel.my.balance.toPlainString()) }
          Td { Text(channel.their.balance.toPlainString()) }
          Td {
            if (channel.status == "OPEN") {
              ChannelTransfers(channel)
              Br()
            }
            Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList()) {
              channels[index] = it
            }
          }
        }
      }
    }
  }
}

suspend fun MutableList<Channel>.load() {
  val newChannels = getChannels().map { channel ->
    val eltooCoins = MDS.getCoins(address = channel.eltooAddress)
    eltooScriptCoins.put(channel.eltooAddress, eltooCoins)
    if (channel.status == "OPEN" && eltooCoins.isNotEmpty()) updateChannelStatus(channel, "TRIGGERED")
    else if (channel.status in listOf("TRIGGERED", "UPDATED") && eltooCoins.isEmpty()) updateChannelStatus(channel, "SETTLED")
    else channel
  }
  clear()
  addAll(newChannels)
}