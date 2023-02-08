package ui

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import logic.PaymentRequestSent
import logic.events
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.request
import ltd.mbor.minipay.common.send
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope
import view

@Composable
fun ChannelTransfers(channel: Channel) {
  if (channel.my.balance > ZERO) {
    Br()
    var amount by remember { mutableStateOf(ZERO) }
    DecimalNumberInput(amount, min = ZERO, max = channel.my.balance) { it?.let { amount = it } }
    var isSending by remember { mutableStateOf(false) }
    Button({
      if (isSending) disabled()
      onClick {
        isSending = true
        scope.launch {
          channel.send(amount)
          amount = ZERO
          isSending = false
        }
      }
    }) {
      Text("Send via channel")
    }
  }
  if (channel.their.balance > ZERO) {
    Br()
    var amount by remember { mutableStateOf(ZERO) }
    DecimalNumberInput(amount, min = ZERO, max = channel.their.balance) { it?.let { amount = it } }
    var isSending by remember { mutableStateOf(false) }
    Button({
      if (isSending) disabled()
      onClick {
        isSending = true
        scope.launch {
          val (updateTxAndId, settleTxAndId) = channel.request(amount)
          events += PaymentRequestSent(
            channel,
            updateTxAndId.second,
            settleTxAndId.second,
            channel.sequenceNumber + 1,
            channel.my.balance + amount to channel.their.balance - amount,
          )
          isSending = false
          view = "Channel events"
        }
      }
    }) {
      Text("Request via channel")
    }
  }
}