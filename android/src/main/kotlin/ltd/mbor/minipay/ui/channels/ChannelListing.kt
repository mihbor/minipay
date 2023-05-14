package ltd.mbor.minipay.ui.channels

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
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.channelService
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.preview.fakeEltooCoins
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelListing(
  channels: MutableList<Channel>,
  balances: Map<String, Balance>,
  eltooScriptCoins: MutableMap<String, List<Coin>>,
  activity: MainActivity?,
  setChannel: (Channel) -> Unit,
) {
  LaunchedEffect("channels") {
    channelService.reloadChannels(eltooScriptCoins)
  }
  LazyColumn {
    item {
      Row {
        Button(onClick = {
          scope.launch {
            channelService.reloadChannels(eltooScriptCoins)
          }
        }) {
          Text("Refresh")
        }
      }
    }
    item {
      ChannelTable(channels, balances, eltooScriptCoins, activity, setChannel)
    }
  }
}

@Composable @Preview
fun PreviewChannelListing() {
  MiniPayTheme {
    Column {
      ChannelListing(
        mutableListOf(fakeChannelOpen, fakeChannelOpen.copy(tokenId = "0x00", status = "TRIGGERED", eltooAddress = "Mx999", sequenceNumber = 3, updateTx = "abc")),
        fakeBalances,
        fakeEltooCoins,
        null,
        {}
      )
    }
  }
}
