package ui.channels

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.balances
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MinimaException
import ltd.mbor.minipay.common.completeSettlement
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.postUpdate
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.common.triggerSettlement
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import ui.TokenIcon

@Composable
fun Settlement(channel: Channel, blockNumber: Int, eltooScriptCoins: List<Coin>, updateChannel: (Channel) -> Unit) {

  var settlementTriggering by remember { mutableStateOf(false) }
  var updatePosting by remember { mutableStateOf(false) }
  var settlementCompleting by remember { mutableStateOf(false) }

  Div {
    if (channel.status == "OPEN") {
      Button({
        onClick {
          if (window.confirm("Initiate channel settlement on-chain?")) {
            settlementTriggering = true
            scope.launch {
              try {
                updateChannel(channel.triggerSettlement())
              } catch (e: MinimaException) {
                e.message?.let(window::alert)
              }
              settlementTriggering = false
            }
          }
        }
        if (settlementTriggering) disabled()
      }) {
        Text("Trigger settlement!")
      }
    }
    if (eltooScriptCoins.isNotEmpty()) {
      eltooScriptCoins.firstOrNull()?.let { coin ->
        Br()
        TokenIcon(coin.tokenId, balances)
        Text("Timelock ${
          (coin.created + channel.timeLock - blockNumber).takeIf { it > 0 }?.let { "ends in $it blocks \uD83D\uDD51" } ?: "ended"
        }")
        if (channel.status in listOf("TRIGGERED", "UPDATED")) {
          if (channel.sequenceNumber > coin.state.first { it.port == 99 }.data.toInt()) { // sequence number isn't the latest, can post update
            Br()
            if (channel.updateTx.isNotEmpty()) Button({
              onClick {
                updatePosting = true
                scope.launch {
                  try {
                    updateChannel(channel.postUpdate())
                  } catch (e: MinimaException) {
                    e.message?.let(window::alert)
                  }
                  updatePosting = false
                }
              }
              if (updatePosting) disabled()
            }) {
              Text("Post latest update")
            }
          }
          Button({
            if (settlementCompleting || updatePosting || eltooScriptCoins.any { it.created + channel.timeLock > blockNumber }) disabled()
            onClick {
              if (window.confirm("Finalize channel settlement on-chain?")) {
                settlementCompleting = true
                scope.launch {
                  try {
                    updateChannel(channel.completeSettlement())
                  } catch (e: MinimaException) {
                    e.message?.let(window::alert)
                  }
                  settlementCompleting = false
                }
              }
            }
          }) {
            Text("Complete settlement!")
          }
        }
      }
    }
  }
}