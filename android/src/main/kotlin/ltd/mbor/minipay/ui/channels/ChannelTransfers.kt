package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
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
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.R
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.model.Transport.FIREBASE
import ltd.mbor.minipay.common.model.Transport.NFC
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.channelService
import ltd.mbor.minipay.logic.events
import ltd.mbor.minipay.sendDataToService
import ltd.mbor.minipay.ui.DecimalNumberField
import ltd.mbor.minipay.ui.preview.fakeBalances
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.theme.MiniPayTheme
import ltd.mbor.minipay.view

@Composable
fun ChannelTransfers(channel: Channel, balances: Map<String, Balance>, activity: MainActivity?) {
  ProvideTextStyle(value = TextStyle(fontSize = 10.sp, textAlign = TextAlign.Right)) {
    if (channel.my.balance > ZERO) Row {
      var amount by remember { mutableStateOf(ZERO) }
      var isSending by remember { mutableStateOf(false) }
      var confirmSending by remember { mutableStateOf(false) }
      fun send() {
        isSending = true
        confirmSending = false
        scope.launch {
          with(channelService) { channel.send(amount) }
          isSending = false
          amount = ZERO
        }
      }
      if (confirmSending) AlertDialog(
        onDismissRequest = { confirmSending = false },
        title = {
          Text("Sending confirmation")
        },
        text = {
          Text("Send ${amount.toPlainString()} ${balances[channel.tokenId]?.tokenName ?: "[${channel.tokenId}]"}?")
        },
        confirmButton = {
          Button(::send) {
            Text("Send")
          }
        },
        dismissButton = {
          Button({ confirmSending = false }) {
            Text("Cancel")
          }
        }
      )
      DecimalNumberField(
        amount,
        Modifier.width(100.dp).height(45.dp),
        min = ZERO,
        max = channel.my.balance
      ) { it?.let { amount = it } }
      Button(
        onClick = {
          confirmSending = true
        },
        enabled = !isSending && amount > ZERO
      ) {
        Text(if (isSending) "Sending" else "Send", Modifier.width(65.dp))
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
            val (updateTxAndId, settleTxAndId) = with(channelService) { channel.request(amount) }
            events.add(
              PaymentRequestSent(
                channel,
                updateTxAndId.second,
                settleTxAndId.second,
                channel.sequenceNumber + 1,
                channel.my.balance + amount to channel.their.balance - amount,
                FIREBASE
              )
            )
            preparingRequest = false
            view = "Channel Events"
          }
        },
        enabled = !preparingRequest && amount > ZERO
      ) {
        Text(if (preparingRequest) "Preparing..." else "Request", Modifier.width(65.dp))
      }
      Button(
        onClick = {
          preparingRequest = true
          scope.launch {
            val (updateTxAndId, settleTxAndId) = with(channelService) {  channel.request(amount) }
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
                  NFC
                )
              )
              preparingRequest = false
              view = "Channel Events"
            }
          }
        },
        enabled = !preparingRequest,
        contentPadding = PaddingValues(16.dp, 2.dp)
      ) {
        Image(painterResource(R.drawable.contactless_24), "contactless")
        Text(if (preparingRequest) "Preparing..." else " Request")
      }
    }
  }
}

@Composable @Preview
fun PreviewTransfers() {
  MiniPayTheme {
    Column(Modifier.width(350.dp)) {
      ChannelTransfers(fakeChannelOpen, fakeBalances, null)
    }
  }
}

