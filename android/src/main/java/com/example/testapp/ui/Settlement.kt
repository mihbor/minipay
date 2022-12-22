package com.example.testapp.ui

import android.util.Log
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.testapp.Channel
import com.example.testapp.TAG
import com.example.testapp.logic.completeSettlement
import com.example.testapp.logic.postUpdate
import com.example.testapp.logic.triggerSettlement
import com.example.testapp.scope
import com.example.testapp.ui.preview.fakeChannel
import com.example.testapp.ui.theme.TestAppTheme
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Coin

@Composable
fun Settlement(channel: Channel, blockNumber: Int, eltooScriptCoins: List<Coin>, updateChannel: (Channel) -> Unit) {

  var settlementTriggering by remember { mutableStateOf(false) }
  var updatePosting by remember { mutableStateOf(false) }
  var settlementCompleting by remember { mutableStateOf(false) }
  Log.i(TAG, "Channel status: " + channel.status)

  if (channel.status == "OPEN") {
    Button(
      onClick = {
        settlementTriggering = true
        scope.launch {
          updateChannel(channel.triggerSettlement())
          settlementTriggering = false
        }
      },
      enabled = !settlementTriggering
    ) {
      Text("Trigger settlement!", fontSize = 10.sp)
    }
  }
  if (eltooScriptCoins.isNotEmpty()) {
    eltooScriptCoins.forEach {
//      Br()
      Text("[${it.tokenId}] token eltoo coin: ${it.tokenAmount.toPlainString()} timelock ${
        (it.created.toInt() + channel.timeLock - blockNumber).takeIf { it > 0 }?.let { "ends in $it blocks" } ?: "ended"}",
        fontSize = 8.sp
      )
    }
    if (channel.status == "TRIGGERED" && channel.sequenceNumber > 0) {
//      Br()
      if (channel.updateTx.isNotEmpty()) Button(
        onClick = {
          updatePosting = true
          scope.launch {
            updateChannel(channel.postUpdate())
            updatePosting = false
          }
        },
        enabled = !updatePosting
      ) {
        Text("Post latest update", fontSize = 10.sp)
      }
    }
    if (channel.status in listOf("TRIGGERED", "UPDATED")) {
      Button(
        enabled = !settlementCompleting && !updatePosting && eltooScriptCoins.none { it.created.toInt() + channel.timeLock > blockNumber },
        onClick = {
          settlementCompleting = true
          scope.launch {
            updateChannel(channel.completeSettlement())
            settlementCompleting = false
          }
        }
      ) {
        Text("Complete settlement!", fontSize = 10.sp)
      }
    }
  }
}

@Composable
@Preview
fun previewSettlement() {

  TestAppTheme {
    Settlement(channel = fakeChannel, blockNumber = 5, eltooScriptCoins = emptyList(), updateChannel = {})
  }
}