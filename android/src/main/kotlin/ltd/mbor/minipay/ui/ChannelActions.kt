package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.blockNumber
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannel
import ltd.mbor.minipay.ui.preview.fakeEltooCoins
import ltd.mbor.minipay.ui.preview.fakeTriggeredChannel
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelActions(
  channel: Channel,
  balances: Map<String, Balance>,
  eltooScriptCoins: Map<String, List<Coin>>,
  activity: MainActivity?,
  updateChannel: (Channel) -> Unit
) {
  Column(Modifier.fillMaxWidth()) {
    if (channel.status == "OPEN") {
      ChannelTransfers(channel, activity)
    }
    ChannelDetails(channel, balances, activity, updateChannel)
    Settlement(channel, blockNumber, eltooScriptCoins[channel.eltooAddress] ?: emptyList(), updateChannel)
    if (channel.status in setOf("OFFERED", "SETTLED")) {
      DeleteChannel(channel, updateChannel)
    }
  }
}

@Composable @Preview
fun PreviewOpenChannelActions() {
  MiniPayTheme {
    ChannelActions(fakeChannel, fakeBalances, fakeEltooCoins, null) {}
  }
}

@Composable @Preview
fun PreviewTriggeredChannelActions() {
  MiniPayTheme {
    ChannelActions(fakeTriggeredChannel, fakeBalances, fakeEltooCoins, null) {}
  }
}
