package com.example.testapp.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.MainActivity
import com.example.testapp.TAG
import com.example.testapp.scope
import com.example.testapp.sendDataToService
import com.example.testapp.ui.preview.fakeChannel
import com.example.testapp.ui.theme.TestAppTheme
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.launch
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.request
import ltd.mbor.minipay.common.send

@Composable
fun ChannelTransfers(channel: Channel, activity: MainActivity?, setRequestSentOnChannel: (Channel) -> Unit) {
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
          val (updateTx, settleTx) = channel.request(amount)
          activity?.apply {
            disableReaderMode()
            sendDataToService("TXN_REQUEST;$updateTx;$settleTx")
            Log.i(TAG, "TXN_REQUEST sent, updateTxLength: ${updateTx.length}, settleTxLength: ${settleTx.length}")
            setRequestSentOnChannel(channel)
            preparingRequest = false
          }
        }
      },
      enabled = !preparingRequest
    ) {
      Text(if (preparingRequest) "Preparing..." else "Request")
    }
  }
}

@Composable
@Preview
fun PreviewTransfers() {
  TestAppTheme {
    Column {
      ChannelTransfers(fakeChannel, null, {})
    }
  }
}

