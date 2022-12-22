package ui

import Channel
import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import logic.request
import logic.send
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope

@Composable
fun ChannelTransfers(channel: Channel) {
  var isSending by remember { mutableStateOf(false) }
  if (channel.my.balance > ZERO) {
    Br()
    var amount by remember { mutableStateOf(ZERO) }
    DecimalNumberInput(amount, min = ZERO, max = channel.my.balance) { it?.let { amount = it } }
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
    Button({
      onClick {
        scope.launch {
          channel.request(amount)
        }
      }
    }) {
      Text("Request via channel")
    }
  }
}
