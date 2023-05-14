package ui.channels

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import logic.channelService
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChannelRequestReceived(
  channel: Channel,
  updateTxId: Int,
  settleTxId: Int,
  sequenceNumber: Int,
  channelBalance: Pair<BigDecimal, BigDecimal>,
  tokens: Map<String, Token>,
  dismiss: () -> Unit
) {
  var preparingResponse by remember { mutableStateOf(false) }
  val balanceChange = channel.my.balance - channelBalance.first

  Text("Request received to send ${balanceChange.toPlainString()} ${tokens[channel.tokenId]?.name ?: "[${channel.tokenId}]"} over channel ${channel.id}")
  Button({
    onClick {
      dismiss()
    }
  }) {
    Text("Reject")
  }
  Button({
    onClick {
      preparingResponse = true
      scope.launch {
        with(channelService) { channel.acceptRequestAndReply(updateTxId, settleTxId, sequenceNumber, channelBalance) }
        preparingResponse = false
        dismiss()
      }
    }
    if (preparingResponse) disabled()
  }) {
    Text(if (preparingResponse) "Preparing response..." else "Accept")
  }
}