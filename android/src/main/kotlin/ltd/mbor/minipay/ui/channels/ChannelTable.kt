package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.channelService
import ltd.mbor.minipay.ui.TokenIcon
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.preview.fakeChannelTriggered
import ltd.mbor.minipay.ui.preview.fakeEltooCoins
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChannelTable(
  channels: MutableList<Channel>,
  balances: Map<String, Balance>,
  eltooScriptCoins: MutableMap<String, List<Coin>>,
  activity: MainActivity?,
  selectChannel: (Channel) -> Unit,
) {
  ProvideTextStyle(value = TextStyle(fontSize = 10.sp, textAlign = TextAlign.Right)) {
    Row {
      Text("ID", Modifier.width(30.dp))
      Text("Status", Modifier.width(60.dp))
      Text("Seq\nnumber", Modifier.width(50.dp))
      Text("Token", Modifier.width(75.dp))
      Text("My\nbalance", Modifier.width(50.dp))
      Text("Their\nbalance", Modifier.width(50.dp))
      Text("Actions", Modifier.width(40.dp))
    }
    Divider()
    channels.forEach { channel ->
      key(channel.id) {
        var showActions by remember { mutableStateOf(false) }
        Row {
          Text(channel.id.toString(), Modifier.width(30.dp))
          Text(channel.status, Modifier.width(60.dp))
          Text(channel.sequenceNumber.toString(), Modifier.width(50.dp))
          Row(Modifier.width(75.dp)) {
            Text(balances[channel.tokenId]?.tokenName ?: if(channel.tokenId == "0x00") "Minima" else "[${channel.tokenId.take(8)}...]", Modifier.width(60.dp))
            TokenIcon(channel.tokenId, balances, size = 15)
          }
          Text(channel.my.balance.toPlainString(), Modifier.width(50.dp))
          Text(channel.their.balance.toPlainString(), Modifier.width(50.dp))
          CompositionLocalProvider(
            LocalMinimumTouchTargetEnforcement provides false,
          ) {
            IconButton(onClick = { showActions = !showActions }, Modifier.width(40.dp)) {
              Icon(Icons.Filled.List, contentDescription = null)
            }
          }
        }
        if (showActions) Row(Modifier.fillMaxWidth()){
          ChannelActions(channel, balances, activity, selectChannel) {
            scope.launch { channelService.reloadChannels(eltooScriptCoins) }
          }
        }
        Divider()
      }
    }
  }
}

@Composable @Preview
fun PreviewChannelTable() {
  MiniPayTheme {
    Column {
      ChannelTable(
        mutableListOf(fakeChannelOpen, fakeChannelTriggered.copy(tokenId = "0x00")),
        fakeBalances,
        fakeEltooCoins,
        null,
        {}
      )
    }
  }
}

@Composable @Preview
fun PreviewChannelTableNoBalances() {
  MiniPayTheme {
    Column {
      ChannelTable(
        mutableListOf(fakeChannelOpen, fakeChannelTriggered.copy(tokenId = "0x00")),
        emptyMap(),
        fakeEltooCoins,
        null,
        {}
      )
    }
  }
}
