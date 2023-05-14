package ui.channels

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.channelService
import logic.events
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.model.Transport.FIREBASE
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import ui.DecimalNumberInput
import view

@Composable
fun ChannelTransfers(channel: Channel, balances: Map<String, Balance>) {
  if (channel.my.balance > ZERO) Div{
    var amount by remember { mutableStateOf(ZERO) }
    DecimalNumberInput(amount, min = ZERO, max = channel.my.balance) { it?.let { amount = it } }
    var isSending by remember { mutableStateOf(false) }
    Button({
      if (isSending || amount <= ZERO) disabled()
      onClick {
        if (window.confirm("Send ${amount.toPlainString()} ${balances[channel.tokenId]?.tokenName ?: "[${channel.tokenId}]"}?")) {
          isSending = true
          scope.launch {
            with(channelService) { channel.send(amount) }
            amount = ZERO
            isSending = false
          }
        }
      }
    }) {
      Text("Send via channel")
    }
  }
  if (channel.their.balance > ZERO) Div{
    var amount by remember { mutableStateOf(ZERO) }
    DecimalNumberInput(amount, min = ZERO, max = channel.their.balance) { it?.let { amount = it } }
    var isSending by remember { mutableStateOf(false) }
    Button({
      if (isSending || amount <= ZERO) disabled()
      onClick {
        isSending = true
        scope.launch {
          val (updateTxAndId, settleTxAndId) = with(channelService) { channel.request(amount) }
          events += PaymentRequestSent(
            channel,
            updateTxAndId.second,
            settleTxAndId.second,
            channel.sequenceNumber + 1,
            channel.my.balance + amount to channel.their.balance - amount,
            FIREBASE
          )
          isSending = false
          view = "Channel Events"
        }
      }
    }) {
      Text("Request via channel")
    }
  }
}
