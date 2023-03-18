package ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.document
import kotlinx.browser.window
import logic.eltooScriptAddress
import logic.multisigScriptAddress
import logic.multisigScriptBalances
import logic.requestChannel
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.model.Channel
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
fun RequestChannel(
  myKeys: Channel.Keys,
  myAddress: String,
  balances: SnapshotStateMap<String, Balance>,
  tokens: SnapshotStateMap<String, Token>,
  useMaxima: Boolean,
  maximaContact: Contact?,
) {
  var myAmount by remember { mutableStateOf(BigDecimal.ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }

  var showQR by remember { mutableStateOf(false) }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }
  
  var progressStep: Int by remember { mutableStateOf(0) }
  
  var channel by remember { mutableStateOf<Channel?>(null) }
  
  LaunchedEffect("requestChannel") {
    triggerTxStatus = ""
    settlementTxStatus = ""
    multisigScriptAddress = ""
    eltooScriptAddress = ""
    multisigScriptBalances.clear()
  }

  fun requestChannel() {
    showQR = !showQR
    val canvas = document.getElementById("joinChannelQR") as HTMLCanvasElement
    QRCode.toCanvas(
      canvas, channelKey(myKeys, tokenId) + ";" + myAmount.toPlainString() + ";" + myAddress
    ) { error ->
      if (error != null) console.error(error)
      else {
        requestChannel(myAddress, myKeys, tokenId, myAmount) { event, newChannel ->
          progressStep++
          when (event) {
            SIGS_RECEIVED -> {
              triggerTxStatus = "Trigger transaction received"
              settlementTxStatus = "Settlement transaction received"
              showQR = false
            }

            TRIGGER_TX_SIGNED -> triggerTxStatus += " and signed"
            SETTLEMENT_TX_SIGNED -> settlementTxStatus += " and signed"
            CHANNEL_PUBLISHED -> {
              channel = newChannel
              triggerTxStatus += " and sent back."
              settlementTxStatus += " and sent back."
            }

            CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED -> {
              channel = newChannel
              progressStep--
            }

            else -> {}
          }
        }
      }
    };Unit
  }
  fun requestMaximaChannel(contact: Contact) {

  }
  fun requestChannel(contact: Contact?) {
    if (contact != null) requestMaximaChannel(contact)
    else requestChannel()
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
    DecimalNumberInput(myAmount, min = BigDecimal.ZERO, disabled = showQR) {
      it?.let { myAmount = it }
    }
    TokenSelect(tokenId, balances, tokens, disabled = showQR) {
      tokenId = it
    }
    Br()
    if (!showQR) Button({
      if (myAmount < 0 || listOf(myKeys.trigger, myKeys.update, myKeys.settle).any{ it.isBlank() }) disabled()
      if (useMaxima && maximaContact == null) disabled()
      onClick {
        if (window.confirm("Fund new channel with ${myAmount.toPlainString()} ${balances[tokenId]?.tokenName ?: "[$tokenId]"}?")) requestChannel(maximaContact)
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
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  channel?.let {
    ChannelView(it, balances) {
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