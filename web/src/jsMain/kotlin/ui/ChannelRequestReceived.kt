package ui

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Transaction
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.acceptRequest
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.publish
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope

@Composable
fun ChannelRequestReceived(
  channel: Channel,
  updateTx: Pair<Int, Transaction>,
  settleTx: Pair<Int, Transaction>,
  dismiss: () -> Unit
) {
  var preparingResponse by remember { mutableStateOf(false) }
  val outputs = settleTx.second.outputs
  val myOutput = outputs.find { it.address == channel.my.address }
  val balanceChange = channel.my.balance - (myOutput?.amount ?: BigDecimal.ZERO)
  
  Text("Request received to send ${balanceChange.toPlainString()} Minima over channel ${channel.id}")
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
        channel.acceptRequest(updateTx, settleTx).let { (updateTx, settleTx) ->
          publish(channelKey(channel.their.keys, channel.tokenId), "TXN_UPDATE_ACK;$updateTx;$settleTx")
        }
        preparingResponse = false
        dismiss()
      }
    }
    if (preparingResponse) disabled()
  }) {
    Text(if (preparingResponse) "Reparing response..." else "Accept")
  }
}