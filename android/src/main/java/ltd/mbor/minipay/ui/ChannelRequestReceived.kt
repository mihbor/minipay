package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.Transaction
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.acceptRequest
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.sendDataToService
import ltd.mbor.minipay.ui.preview.fakeMinimaChannel
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelRequestReceived(
  channel: Channel,
  updateTx: Pair<Int, Transaction>,
  settleTx: Pair<Int, Transaction>,
  tokens: Map<String, Token>,
  activity: MainActivity?,
  dismiss: () -> Unit
) {
  var accepting by remember { mutableStateOf(false) }
  var preparingResponse by remember { mutableStateOf(false) }
  val outputs = settleTx.second.outputs
  val myOutput = outputs.find { it.address == channel.my.address }
  val balanceChange = channel.my.balance - (myOutput?.amount ?: ZERO)

  Column {
    Text("Request received to send ${balanceChange.toPlainString()} ${tokens[channel.tokenId]?.name ?: "[${channel.tokenId}]"} over channel ${channel.id}")
    Button(onClick = {
      accepting = false
      dismiss()
    }) {
      Text(if (accepting) "Finish" else "Reject")
    }
    if(accepting) {
      Text("Use contactless again to complete transaction")
    } else Button(
      onClick = {
        preparingResponse = true
        scope.launch {
          channel.acceptRequest(updateTx, settleTx).let { (updateTx, settleTx) ->
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
fun PreviewChannelRequest() {
  MiniPayTheme {
    ChannelRequestReceived(channel = fakeMinimaChannel, updateTx = 1 to Transaction.empty, settleTx = 2 to Transaction.empty, previewTokens, null) { }
  }
}

val Transaction.Companion.empty get() = Transaction(emptyList(), emptyList(), emptyList(), "", Transaction.Header("1", "2"))