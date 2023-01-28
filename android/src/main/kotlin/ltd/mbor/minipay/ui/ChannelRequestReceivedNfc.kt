package ltd.mbor.minipay.ui

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
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.acceptRequest
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.sendDataToService
import ltd.mbor.minipay.ui.preview.fakeMinimaChannel
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelRequestReceivedNfc(
  channel: Channel,
  updateTxId: Int,
  settleTxId: Int,
  sequenceNumber: Int,
  channelBalance: Pair<BigDecimal, BigDecimal>,
  tokens: Map<String, Token>,
  activity: MainActivity?,
  dismiss: () -> Unit
) {
  var accepting by remember { mutableStateOf(false) }
  var preparingResponse by remember { mutableStateOf(false) }
  val balanceChange = channel.my.balance - channelBalance.first

  Column {
    Text("Request received to send ${balanceChange.toPlainString()} ${tokens[channel.tokenId]?.name ?: "[${channel.tokenId}]"} over channel ${channel.id}")
    Button(onClick = {
      accepting = false
      dismiss()
    }) {
      Text(if (accepting) "Finish" else "Reject")
    }
    if (accepting) {
      Text("Use contactless again to complete transaction")
    } else Button(
      onClick = {
        preparingResponse = true
        scope.launch {
          channel.acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance).let { (updateTx, settleTx) ->
            activity?.apply {
              disableReaderMode()
              sendDataToService("TXN_UPDATE_ACK;$updateTx;$settleTx")
            }
          }
          accepting = true
          preparingResponse = false
        }
      },
      enabled = !preparingResponse
    ) {
      Text(if (preparingResponse) "Preparing response..." else "Accept")
    }
  }
}

@Composable @Preview
fun PreviewChannelRequestNfc() {
  MiniPayTheme {
    ChannelRequestReceivedNfc(channel = fakeMinimaChannel, updateTxId = 1, settleTxId = 2, 5, ZERO to ONE, previewTokens, null) { }
  }
}
