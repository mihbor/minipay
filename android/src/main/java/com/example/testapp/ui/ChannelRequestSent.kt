package com.example.testapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.testapp.ui.theme.TestAppTheme

@Composable
fun ChannelRequestSent(stopEmitting: () -> Unit, dismiss: () -> Unit) {
  var requestSent by remember { mutableStateOf(false) }
  Column {
    if (!requestSent) {
      Text("Use contactless again to initiate transaction, then press Request Sent")
      Button(onClick = {
        stopEmitting()
        requestSent = true
      }) {
        Text(text = "Request Sent")
      }
    } else {
      Text("When counter party accepts, use contactless again to complete transaction")
    }
    Button(onClick = {
      dismiss()
    }) {
      Text(text = "Cancel")
    }
  }
}

@Composable
@Preview
fun PreviewChannelRequestSent() {
  TestAppTheme {
    ChannelRequestSent({}) {}
  }
}