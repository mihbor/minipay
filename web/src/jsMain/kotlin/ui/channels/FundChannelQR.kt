package ui.channels

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import externals.QrScanner
import kotlinx.browser.document
import ltd.mbor.minipay.common.model.Channel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Video
import org.w3c.dom.HTMLVideoElement

@Composable
fun FundChannelQR(noProgressYet: Boolean, onScan: (Channel.Keys, String, BigDecimal, String) -> Unit) {
  var showFundScanner by remember { mutableStateOf(false) }
  var qrScanner: QrScanner? by remember { mutableStateOf(null) }

  if (noProgressYet) {
    Br()
    Button({
      onClick {
        showFundScanner = !showFundScanner
      }
      style {
        if (showFundScanner) border(style = LineStyle.Inset)
      }
    }) {
      Text("Scan QR code")
    }
  }
  Br()
  if (showFundScanner) {
    Video({
      id("fundChannelVideo")
      style {
        width(500.px)
        height(500.px)
        property("pointer-events", "none")
      }
    })
    DisposableEffect("fundChannelVideo") {
      val video = document.getElementById("fundChannelVideo").also { console.log("video", it) } as HTMLVideoElement
      qrScanner = QrScanner(video) { result ->
        console.log("decoded qr code: $result")
        result.split(';').apply {
          onScan(Channel.Keys(this[0], this[1], this[2]), this[3], this[4].toBigDecimal(), this[5])
        }
        qrScanner!!.stop()
        showFundScanner = false
      }.also { it.start() }
      onDispose {
        qrScanner?.stop()
      }
    }
  }
}