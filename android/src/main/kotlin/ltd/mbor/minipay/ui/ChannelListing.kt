package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.logic.reload
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannel
import ltd.mbor.minipay.ui.preview.fakeEltooCoins
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelListing(
  channels: MutableList<Channel>,
  balances: Map<String, Balance>,
  eltooScriptCoins: MutableMap<String, List<Coin>>,
  activity: MainActivity?,
) {
  LaunchedEffect("channels") {
    channels.reload(eltooScriptCoins)
  }
  LazyColumn {
    item {
      Row {
        Button(onClick = {
          scope.launch {
            channels.reload(eltooScriptCoins)
          }
        }) {
          Text("Refresh")
        }
      }
    }
    item {
      ChannelTable(channels, balances, eltooScriptCoins, activity)
    }
  }
}

@Composable @Preview
fun PreviewChannelListing() {
  MiniPayTheme {
    Column {
      ChannelListing(
        mutableListOf(fakeChannel, fakeChannel.copy(tokenId = "0x00", status = "TRIGGERED", eltooAddress = "Mx999", sequenceNumber = 3, updateTx = "abc")),
        fakeBalances,
        fakeEltooCoins,
        null
      )
    }
  }
}
