package ltd.mbor.minipay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
  if (channel.my.balance > ZERO) Row {
    var amount by remember { mutableStateOf(ZERO) }
    var isSending by remember { mutableStateOf(false) }
    DecimalNumberField(
      amount,
      Modifier.width(100.dp).height(45.dp),
      TextStyle(fontSize = 12.sp),
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
      Text("Send", Modifier.width(60.dp))
    }
  }
  if (channel.their.balance > ZERO) Row{
    var amount by remember { mutableStateOf(ZERO) }
    var preparingRequest by remember { mutableStateOf(false) }
    DecimalNumberField(
      amount,
      Modifier.width(100.dp).height(45.dp),
      TextStyle(fontSize = 12.sp),
      min = ZERO,
      max = channel.their.balance
    ) { it?.let { amount = it } }
    Button(
      onClick = {
        preparingRequest = true
        scope.launch {
          val (updateTxAndId, settleTxAndId) = channel.request(amount)
          events.add(PaymentRequestSent(
            channel,
            updateTxAndId.second,
            settleTxAndId.second,
            channel.sequenceNumber + 1,
            channel.my.balance + amount to channel.their.balance - amount,
            isNfc = false
          ))
          preparingRequest = false
          view = "events"
        }
      },
      enabled = !preparingRequest
    ) {
      Text(if (preparingRequest) "Preparing..." else "Request")
    }
    Button(
      onClick = {
        preparingRequest = true
        scope.launch {
          val (updateTxAndId, settleTxAndId) = channel.request(amount)
          activity?.apply {
            disableReaderMode()
            sendDataToService("TXN_REQUEST;${updateTxAndId.first};${settleTxAndId.first}")
            events.add(PaymentRequestSent(
              channel,
              updateTxAndId.second,
              settleTxAndId.second,
              channel.sequenceNumber + 1,
              channel.my.balance + amount to channel.their.balance - amount,
              isNfc = false
            ))
            preparingRequest = false
            view = "events"
          }
        }
      },
      enabled = !preparingRequest
    ) {
      Image(painterResource(R.drawable.contactless_24), "contactless")
      Text(if (preparingRequest) "Preparing..." else " Request")
    }
  }
}

@Composable
@Preview
fun PreviewTransfers() {
  MiniPayTheme {
    Column {
      ChannelTransfers(fakeChannel, null)
    }
  }
}

