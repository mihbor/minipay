package ltd.mbor.minipay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import ltd.mbor.minipay.*
import ltd.mbor.minipay.R
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.request
import ltd.mbor.minipay.common.send
import ltd.mbor.minipay.logic.PaymentRequestSent
import ltd.mbor.minipay.logic.events
import ltd.mbor.minipay.ui.preview.fakeChannel
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelTransfers(channel: Channel, activity: MainActivity?) {
  val fontSize = 10.sp
  ProvideTextStyle(value = TextStyle(fontSize = fontSize, textAlign = TextAlign.Right)) {
    if (channel.my.balance > ZERO) Row {
      var amount by remember { mutableStateOf(ZERO) }
      var isSending by remember { mutableStateOf(false) }
      DecimalNumberField(
        amount,
        Modifier.width(100.dp).height(45.dp),
        min = ZERO,
        max = channel.my.balance
      ) { it?.let { amount = it } }
      Button(
        onClick = {
          isSending = true
          scope.launch {
            channel.send(amount)
            isSending = false
          }
        },
        enabled = !isSending
      ) {
        Text("Send", Modifier.width(50.dp), fontSize = fontSize)
      }
    }
    if (channel.their.balance > ZERO) Row {
      var amount by remember { mutableStateOf(ZERO) }
      var preparingRequest by remember { mutableStateOf(false) }
      DecimalNumberField(
        amount,
        Modifier.width(100.dp).height(45.dp),
        min = ZERO,
        max = channel.their.balance
      ) { it?.let { amount = it } }
      Button(
        onClick = {
          preparingRequest = true
          scope.launch {
            val (updateTxAndId, settleTxAndId) = channel.request(amount)
            events.add(
              PaymentRequestSent(
                channel,
                updateTxAndId.second,
                settleTxAndId.second,
                channel.sequenceNumber + 1,
                channel.my.balance + amount to channel.their.balance - amount,
                isNfc = false
              )
            )
            preparingRequest = false
            view = "Channel events"
          }
        },
        enabled = !preparingRequest
      ) {
        Text(if (preparingRequest) "Preparing..." else "Request", Modifier.width(50.dp), fontSize = fontSize)
      }
      Button(
        onClick = {
          preparingRequest = true
          scope.launch {
            val (updateTxAndId, settleTxAndId) = channel.request(amount)
            activity?.apply {
              disableReaderMode()
              sendDataToService("TXN_REQUEST;${updateTxAndId.first};${settleTxAndId.first}")
              events.add(
                PaymentRequestSent(
                  channel,
                  updateTxAndId.second,
                  settleTxAndId.second,
                  channel.sequenceNumber + 1,
                  channel.my.balance + amount to channel.their.balance - amount,
                  isNfc = false
                )
              )
              preparingRequest = false
              view = "Channel events"
            }
          }
        },
        enabled = !preparingRequest,
        contentPadding = PaddingValues(10.dp, 2.dp)
      ) {
        Image(painterResource(R.drawable.contactless_24), "contactless")
        Text(if (preparingRequest) "Preparing..." else " Request", fontSize = fontSize)
      }
    }
  }
}

@Composable @Preview
fun PreviewTransfers() {
  MiniPayTheme {
    Column(Modifier.width(350.dp)) {
      ChannelTransfers(fakeChannel, null)
    }
  }
}

