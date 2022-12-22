package ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import externals.QrScanner
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.isPaymentChannelAvailable
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.send
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLVideoElement
import scope

@Composable
fun Send(balances: SnapshotStateMap<String, Balance>) {
  
  var showCam by remember { mutableStateOf(false) }
  var sending by remember { mutableStateOf(false) }
  var toAddress by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf(ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }
  var qrScanner: QrScanner? by remember { mutableStateOf(null) }
  
  TextInput(toAddress) {
    onInput {
      toAddress = it.value
    }
    style {
      width(400.px)
    }
  }
  Br()
  DecimalNumberInput(amount, min = ZERO) {
    it?.let { amount = it }
  }
  TokenIcon(tokenId, logic.balances)
  TokenSelect(tokenId, balances) {
    tokenId = it
  }
  Button({
    onClick {
      console.log("nfc read")
      window.open("minipay://localhost:9004/read?uid=${MDS.minidappuid}")
    }
  }) {
    Text("Read NFC (in Android app)")
  }
  Button({
    if (amount <= 0 || toAddress.isEmpty() || sending) disabled()
    onClick {
      sending = true
      console.log("post $amount [$tokenId] to $toAddress")
      scope.launch {
        if (isPaymentChannelAvailable(toAddress, tokenId, amount) && window.confirm("Found available payment channel. Send in channel instead?")) {
          //TODO: pay in channel instead
        } else {
          MDS.send(toAddress, amount, tokenId)
        }
        showCam = false
        sending = false
        qrScanner?.stop()
      }
    }
  }) {
    Text("Send!")
  }
  Br()
  Button({
    onClick {
      showCam = !showCam
    }
    style {
      if (showCam) border(style = LineStyle.Inset)
    }
  }) {
    Text("Scan QR code")
  }
  Br()
  if (showCam) {
    Video({
      id("sendVideo")
      style {
        width(500.px)
        height(500.px)
        property("pointer-events", "none")
      }
    })
    DisposableEffect("sendVideo") {
      val video = document.getElementById("sendVideo").also { console.log("video", it) } as HTMLVideoElement
      qrScanner = QrScanner(video) { result ->
        console.log("decoded qr code: $result")
        val splits = result.split(";")
        toAddress = splits[0]
        if (splits.size > 1 && splits[1].isNotEmpty()) tokenId = splits[1]
        if (splits.size > 2 && splits[2].isNotEmpty()) splits[2].toBigDecimalOrNull()?.let { amount = it }
        qrScanner!!.stop()
        showCam = false
      }.also { it.start() }
      onDispose {
        console.log("qrScanner", qrScanner)
        qrScanner?.stop()
      }
    }
  }
}