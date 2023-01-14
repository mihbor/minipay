package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.blockNumber
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.logic.eltooScriptCoins
import ltd.mbor.minipay.logic.multisigScriptBalances
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannel

@Composable
fun ChannelView(
  channel: Channel,
  balances: Map<String, Balance>,
  activity: MainActivity?,
  setRequestSentOnChannel: (Channel) -> Unit,
  updateChannel: (Channel) -> Unit
) {
//  multisigScriptBalances.firstOrNull{ it.tokenId == channel.tokenId }?.let{
//    TokenIcon(it.tokenId, balances)
//    Text("${it.tokenName} token funding balance: ${it.confirmed.toPlainString()}")
//  }
//  if (multisigScriptBalances.any { it.unconfirmed > ZERO || it.confirmed > ZERO }) {
//    Text("Channel balance: me ${channel.my.balance.toPlainString()}, counterparty ${channel.their.balance.toPlainString()}")
//    ChannelTransfers(channel, activity, setRequestSentOnChannel)
//  }
  Column{
    Row{
//      Text("Status: ${channel.status}", Modifier.width(140.dp))
      TokenIcon(channel.tokenId, balances)
      Text(balances[channel.tokenId]?.tokenName ?: "[${channel.tokenId}]")
    }
    Row {
      Text("My balance: ")
      Text(channel.my.balance.toPlainString(), Modifier.width(60.dp))
      Text("Their balance: ")
      Text(channel.their.balance.toPlainString(), Modifier.width(60.dp))
    }
    Settlement(
      channel.copy(status = if (multisigScriptBalances.any { it.unconfirmed > ZERO || it.confirmed > ZERO }) "OPEN" else channel.status),
      blockNumber,
      eltooScriptCoins[channel.eltooAddress] ?: emptyList(),
      updateChannel
    )
    ChannelTransfers(channel, activity, setRequestSentOnChannel)
  }
}

@Composable @Preview
fun PreviewChannelView() {
  ChannelView(channel = fakeChannel, balances = fakeBalances, activity = null, setRequestSentOnChannel = {}, updateChannel = {})
}