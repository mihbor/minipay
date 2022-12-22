package ui

import Channel
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import logic.balances
import logic.completeSettlement
import logic.postUpdate
import logic.triggerSettlement
import ltd.mbor.minimak.Coin
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import scope

@Composable
fun Settlement(channel: Channel, blockNumber: Int, eltooScriptCoins: List<Coin>, updateChannel: (Channel) -> Unit) {

  var settlementTriggering by remember { mutableStateOf(false) }
  var updatePosting by remember { mutableStateOf(false) }
  var settlementCompleting by remember { mutableStateOf(false) }

  if (channel.status == "OPEN") {
    Button({
      onClick {
        settlementTriggering = true
        scope.launch {
          updateChannel(channel.triggerSettlement())
          settlementTriggering = false
        }
      }
      if (settlementTriggering) disabled()
    }) {
      Text("Trigger settlement!")
    }
  }
  if (eltooScriptCoins.isNotEmpty()) {
    eltooScriptCoins.forEach {
      Br()
      TokenIcon(it.tokenId, balances)
      Text("${balances[it.tokenId]?.tokenName ?: "[${it.tokenId}]"} token eltoo coin: ${it.tokenAmount.toPlainString()} timelock ${
        (it.created.toInt() + channel.timeLock - blockNumber).takeIf { it > 0 }?.let { "ends in $it blocks" } ?: "ended"}"
      )
    }
    if (channel.status == "TRIGGERED" && channel.sequenceNumber > 0) {
      Br()
      if (channel.updateTx.isNotEmpty()) Button({
        onClick {
          updatePosting = true
          scope.launch {
            updateChannel(channel.postUpdate())
            updatePosting = false
          }
        }
        if (updatePosting) disabled()
      }) {
        Text("Post latest update")
      }
    }
    if (channel.status in listOf("TRIGGERED", "UPDATED")) {
      Button({
        if (settlementCompleting || updatePosting || eltooScriptCoins.any { it.created.toInt() + channel.timeLock > blockNumber }) disabled()
        onClick {
          settlementCompleting = true
          scope.launch {
            updateChannel(channel.completeSettlement())
            settlementCompleting = false
          }
        }
      }) {
        Text("Complete settlement!")
      }
    }
  }
}