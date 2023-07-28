package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.channelService
import ltd.mbor.minipay.ui.preview.fakeMinimaChannel
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

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

  Column {
    Text("Request received to send ${balanceChange.toPlainString()} ${tokens[channel.tokenId]?.name ?: "[${channel.tokenId}]"} over channel ${channel.name}")
    Button(onClick = {
      dismiss()
    }) {
      Text("Reject")
    }
    Button(
      onClick = {
        preparingResponse = true
        scope.launch {
          with(channelService) {
            channel.acceptRequestAndReply(updateTxId, settleTxId, sequenceNumber, channelBalance)
          }
          preparingResponse = false
          dismiss()
        }
      },
      enabled = !preparingResponse
    ) {
      Text(if (preparingResponse) "Preparing response..." else "Accept")
    }
  }
}

@Composable @Preview
fun PreviewChannelRequest() {
  MiniPayTheme {
    ChannelRequestReceived(channel = fakeMinimaChannel, updateTxId = 1, settleTxId = 2, 5, ZERO to ONE, previewTokens) { }
  }
}
