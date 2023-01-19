package ui

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
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
  updateTxId: Int,
  settleTxId: Int,
  sequenceNumber: Int,
  channelBalance: Pair<BigDecimal, BigDecimal>,
  dismiss: () -> Unit
) {
  var preparingResponse by remember { mutableStateOf(false) }
  val balanceChange = channel.my.balance - channelBalance.first
  
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
        channel.acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance).let { (updateTx, settleTx) ->
          publish(channelKey(channel.their.keys, channel.tokenId), "TXN_UPDATE_ACK;$updateTx;$settleTx")
        }
        preparingResponse = false
        dismiss()
      }
    }
    if (preparingResponse) disabled()
  }) {
    Text(if (preparingResponse) "Preparing response..." else "Accept")
  }
}