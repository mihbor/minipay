package ui.channels

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.browser.document
import kotlinx.browser.window
import logic.*
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
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
import ui.Canvas
import ui.DecimalNumberInput
import ui.QRCode
import ui.TokenSelect

@Composable
fun RequestChannel(
  myKeys: Channel.Keys,
  myAddress: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  useMaxima: Boolean,
  maximaContact: Contact?,
) {
  var myAmount by remember { mutableStateOf(ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }

  var showQR by remember { mutableStateOf(false) }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }
  
  var progressStep: Int by remember { mutableStateOf(0) }
  
  LaunchedEffect("requestChannel") {
    triggerTxStatus = ""
    settlementTxStatus = ""
    multisigScriptAddress = ""
    eltooScriptAddress = ""
    multisigScriptBalances.clear()
  }

  fun requestChannel(maximaContact: Contact?) {
    requestChannel(ChannelInvite(tokenId, myAddress, myAmount, myKeys, null), maximaContact) { event, newChannel ->
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
          requestedChannel = newChannel
          triggerTxStatus += " and sent back."
          settlementTxStatus += " and sent back."
        }

        CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED -> {
          requestedChannel = newChannel
          progressStep--
        }

        else -> {}
      }
    }
  }

  fun showQR() {
    showQR = true
    val canvas = document.getElementById("joinChannelQR") as HTMLCanvasElement
    QRCode.toCanvas(
      canvas, channelKey(myKeys, tokenId) + ";" + myAmount.toPlainString() + ";" + myAddress
    ) { error ->
      if (error != null) console.error(error)
      else requestChannel(null)
    };Unit
  }

  fun requestChannel() {
    if (useMaxima) requestChannel(maximaContact)
    else showQR()
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
    Text("My contribution:")
    DecimalNumberInput(myAmount, min = ZERO, disabled = showQR) {
      it?.let { myAmount = it }
    }
    TokenSelect(tokenId, balances, tokens, disabled = showQR) {
      tokenId = it
    }
    Br()
    if (!showQR) Button({
      if (myAmount < ZERO || listOf(myKeys.trigger, myKeys.update, myKeys.settle).any{ it.isBlank() }) disabled()
      if (useMaxima && maximaContact == null) disabled()
      onClick {
        if (window.confirm("Fund new channel with ${myAmount.toPlainString()} ${balances[tokenId]?.tokenName ?: "[$tokenId]"}?")) requestChannel()
      }
    }) {
      Text("Request channel")
    } else Button({
      onClick {
        showQR = false
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
  requestedChannel?.let { channel ->
    ChannelView(channels[channel.id] ?: channel, balances) {
      requestedChannel = it
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