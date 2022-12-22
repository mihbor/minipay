package ui

import Channel
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.document
import logic.JoinChannelEvent.*
import logic.channelKey
import logic.joinChannel
import logic.newKeys
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getAddress
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Progress
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLCanvasElement

@Composable
fun RequestChannel(balances: SnapshotStateMap<String, Balance>, tokens: SnapshotStateMap<String, Token>) {
  var myAddress by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf(BigDecimal.ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }
  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  
  var showQR by remember { mutableStateOf(false) }
  var triggerTxStatus by remember { mutableStateOf("") }
  var updateTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }
  
  var progressStep: Int by remember { mutableStateOf(0) }
  
  var channel by remember { mutableStateOf<Channel?>(null) }
  
  LaunchedEffect("requestChannel") {
    newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    myAddress = MDS.getAddress()
  }
  fun requestChannel() {
    triggerTxStatus = ""
    settlementTxStatus = ""
    val canvas = document.getElementById("joinChannelQR") as HTMLCanvasElement
    QRCode.toCanvas(
      canvas, channelKey(myKeys, tokenId) + ";" + amount.toPlainString() + ";" + myAddress
    ) { error ->
      if (error != null) console.error(error)
      else {
        joinChannel(myAddress, myKeys, tokenId, amount) { event, newChannel ->
          progressStep++
          when (event) {
            SIGS_RECEIVED -> {
              triggerTxStatus = "Trigger transaction received"
              settlementTxStatus = "Settlement transaction received"
            }
            TRIGGER_TX_SIGNED -> triggerTxStatus += " and signed"
            SETTLEMENT_TX_SIGNED -> settlementTxStatus += " and signed"
            CHANNEL_PUBLISHED -> {
              channel = newChannel
              triggerTxStatus += " and sent back."
              settlementTxStatus += " and sent back."
            }
            CHANNEL_UPDATED -> {
              channel = newChannel
              updateTxStatus += "Update transaction received. "
            }
            CHANNEL_UPDATED_ACKED -> {
              channel = newChannel
              updateTxStatus += "Update transaction ack received. "
            }
            else -> {}
          }
        }
      }
    };Unit
  }
  Br()
  if (progressStep > 0) {
    Progress({
      attr("value", progressStep.toString())
      attr("max", 6.toString())
      style {
        width(500.px)
      }
    })
    Br()
  }
  if (triggerTxStatus.isEmpty()) {
    Text("Trigger key: ${myKeys.trigger}")
    Br()
    Text("Update key: ${myKeys.update}")
    Br()
    Text("Settlement key: ${myKeys.settle}")
    Br()
    DecimalNumberInput(amount, min = BigDecimal.ZERO, disabled = showQR) {
      it?.let { amount = it }
    }
    TokenSelect(tokenId, balances, tokens, disabled = showQR) {
      tokenId = it
    }
    Br()
    if (!showQR) Button({
      if (amount < 0 || listOf(myKeys.trigger, myKeys.update, myKeys.settle).any{ it.isBlank() }) disabled()
      onClick {
        showQR = !showQR
        requestChannel()
      }
    }) {
      Text("Request channel")
    } else Button({
      onClick {
        showQR = !showQR
      }
    }) {
      Text("Cancel")
    }
  }
  triggerTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  updateTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  channel?.let {
    ChannelView(it) {
      channel = it
    }
  }
  Br()
  Canvas({
    id("joinChannelQR")
    style {
      if (!showQR) display(DisplayStyle.None)
    }
  })
  if (showQR) {
    Br()
    Text("Scan QR code on counter party device")
  }
}