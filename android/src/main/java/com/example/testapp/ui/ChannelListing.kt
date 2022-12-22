package com.example.testapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.*
import com.example.testapp.logic.channels
import com.example.testapp.logic.eltooScriptCoins
import com.example.testapp.logic.getChannels
import com.example.testapp.logic.updateChannelStatus
import com.example.testapp.ui.preview.fakeBalances
import com.example.testapp.ui.preview.fakeChannel
import com.example.testapp.ui.theme.TestAppTheme
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getCoins

@Composable
fun ChannelListing(activity: MainActivity?, setRequestSentOnChannel: (Channel) -> Unit) {
  LaunchedEffect("channels") {
    channels.load()
  }

  if (inited) {
    LazyColumn {
      item {
        Row {
          Button(onClick = {
            scope.launch {
              channels.load()
            }
          }) {
            Text("Refresh")
          }
        }
      }
      item {
        ChannelTable(channels, balances, eltooScriptCoins, activity, setRequestSentOnChannel) { index, channel ->
          channels[index] = channel
        }
      }
    }
  }
}

@Composable
fun ChannelTable(
  channels: List<Channel>,
  balances: Map<String, Balance>,
  eltooScriptCoins: Map<String, List<Coin>>,
  activity: MainActivity?,
  setRequestSentOnChannel: (Channel) -> Unit,
  updateChannel: (Int, Channel) -> Unit
) {
  ProvideTextStyle(value = TextStyle(fontSize = 10.sp)) {
    Row {
      Text("ID", Modifier.width(30.dp))
      Text("Status", Modifier.width(60.dp))
      Text("Seq\nnumber", Modifier.width(50.dp))
      Text("Token", Modifier.width(50.dp))
      Text("My\nbalance", Modifier.width(50.dp))
      Text("Their\nbalance", Modifier.width(50.dp))
      Text("Actions")
    }
  }
  ProvideTextStyle(value = TextStyle(fontSize = 10.sp, textAlign = TextAlign.Right)) {
    channels.forEachIndexed { index, channel ->
      key(channel.id) {
        var showActions by remember { mutableStateOf(false) }
        Row {
          Text(channel.id.toString(), Modifier.width(30.dp))
          Text(channel.status, Modifier.width(60.dp))
          Text(channel.sequenceNumber.toString(), Modifier.width(50.dp))
          Text(balances[channel.tokenId]?.tokenName ?: "???", Modifier.width(50.dp))
          Text(channel.my.balance.toPlainString(), Modifier.width(50.dp))
          Text(channel.their.balance.toPlainString(), Modifier.width(50.dp))
          IconButton(onClick = { showActions = !showActions }) {
            Icon(Icons.Filled.List, contentDescription = null)
          }
        }
        if (showActions) Row {
          Column(Modifier.width(250.dp)) {
            if (channel.status == "OPEN") {
              ChannelTransfers(channel, activity, setRequestSentOnChannel)
            }
            Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList()) {
              updateChannel(index, it)
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

@Composable
@Preview
fun PreviewChannelListing() {
  TestAppTheme {
    ChannelListing(null, {})
  }
}

@Composable @Preview
fun PreviewChannelTable() {
  TestAppTheme {
    Column {
      ChannelTable(
        listOf(fakeChannel, fakeChannel.copy(status = "TRIGGERED", eltooAddress = "Mx999", sequenceNumber = 3, updateTx = "abc")),
        fakeBalances,
        mapOf("Mx999" to listOf(Coin(address = "", miniAddress = "", amount = BigDecimal.ONE, coinId = "", storeState = true, tokenId = "0x00", _created = "100", state = emptyList()))),
        null,
        {}
      ) { _, _ -> }
    }
  }
}