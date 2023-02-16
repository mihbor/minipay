package ltd.mbor.minipay.ui

import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MinimaException
import ltd.mbor.minipay.TAG
import ltd.mbor.minipay.common.completeSettlement
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.postUpdate
import ltd.mbor.minipay.common.triggerSettlement
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.ui.preview.fakeChannelOpen
import ltd.mbor.minipay.ui.preview.fakeChannelTriggered
import ltd.mbor.minipay.ui.preview.fakeCoin
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Settlement(
  channel: Channel,
  blockNumber: Int,
  eltooScriptCoins: List<Coin>,
  updateChannel: (Channel) -> Unit
) {
  
  val context = LocalContext.current
  var settlementTriggering by remember { mutableStateOf(false) }
  var updatePosting by remember { mutableStateOf(false) }
  var settlementCompleting by remember { mutableStateOf(false) }
  Log.i(TAG, "Channel status: " + channel.status)
  
  val fontSize = 10.sp
  ProvideTextStyle(value = TextStyle(fontSize = fontSize)) {
    Column {
      if (channel.status == "OPEN") {
        Button(
          onClick = {
            settlementTriggering = true
            scope.launch {
              try {
                updateChannel(channel.triggerSettlement())
              } catch (e: MinimaException) {
                e.message?.let { Toast.makeText(context, it, LENGTH_LONG).show() }
              }
              settlementTriggering = false
            }
          },
          enabled = !settlementTriggering
        ) {
          Text("Trigger settlement!")
        }
      }
      if (eltooScriptCoins.isNotEmpty()) {
        eltooScriptCoins.firstOrNull()?.let {
          Text("Timelock ${
            (it.created + channel.timeLock - blockNumber).takeIf { it > 0 }?.let { "ends in $it blocks \uD83D\uDD51" } ?: "ended"
          }"
          )
        }
        if (channel.status == "TRIGGERED" && channel.sequenceNumber > 0) {
          if (channel.updateTx.isNotEmpty()) Button(
            onClick = {
              updatePosting = true
              scope.launch {
                try {
                  updateChannel(channel.postUpdate())
                } catch (e: MinimaException) {
                  e.message?.let { Toast.makeText(context, it, LENGTH_LONG).show() }
                }
                updatePosting = false
              }
            },
            enabled = !updatePosting
          ) {
            Text("Post latest update")
          }
        }
        if (channel.status in listOf("TRIGGERED", "UPDATED")) {
          Button(
            enabled = !settlementCompleting && !updatePosting && eltooScriptCoins.none { it.created + channel.timeLock > blockNumber },
            onClick = {
              settlementCompleting = true
              scope.launch {
                try {
                  updateChannel(channel.completeSettlement())
                } catch (e: MinimaException) {
                  e.message?.let { Toast.makeText(context, it, LENGTH_LONG).show() }
                }
                settlementCompleting = false
              }
            }
          ) {
            Text("Complete settlement!")
          }
        }
      }
    }
  }
}

@Composable
@Preview
fun PreviewSettlement() {
  MiniPayTheme {
    Settlement(channel = fakeChannelOpen, blockNumber = 5, eltooScriptCoins = emptyList(), updateChannel = {})
  }
}

@Composable
@Preview
fun PreviewTriggeredSettlement() {
  MiniPayTheme {
    Settlement(channel = fakeChannelTriggered, blockNumber = 5, eltooScriptCoins = listOf(fakeCoin), updateChannel = {})
  }
}