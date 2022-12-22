package ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.document
import kotlinx.browser.window
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getAddress
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLCanvasElement
import require

val QRCode = require("qrcode")

fun drawQR(address: String, tokenId: String, amount:String = "") {
  val canvas = document.getElementById("receiveQR") as HTMLCanvasElement
  QRCode.toCanvas(canvas, "$address;$tokenId;$amount") { error ->
    if (error != null) console.error(error)
    else console.log("qr generated")
  }
}

@Composable
fun Receive(balances: SnapshotStateMap<String, Balance>, tokens: SnapshotStateMap<String, Token>) {
  var myAddress by remember { mutableStateOf("") }
  var tokenId by remember { mutableStateOf("0x00") }
  var amount by remember { mutableStateOf(BigDecimal.ZERO) }
  
  LaunchedEffect("receive") {
    myAddress = MDS.getAddress()
    drawQR(myAddress, tokenId)
  }
  Br()
  Text("My address: $myAddress")
  Br()
  DecimalNumberInput(amount, min = BigDecimal.ZERO) {
    it?.let {
      amount = it
      drawQR(myAddress, tokenId, amount.toPlainString())
    }
  }
  TokenIcon(tokenId, logic.balances)
  TokenSelect(tokenId, balances, tokens) {
    tokenId = it
    drawQR(myAddress, tokenId, amount.toPlainString())
  }
  Button({
    onClick {
      console.log("nfc emit")
      window.open("minipay://localhost:9004/emit?uid=${MDS.minidappuid}&address=$myAddress&token=$tokenId&amount=${amount.toPlainString()}")
    }
  }) {
    Text("Request on NFC (in Android app)")
  }
  Br()
  Canvas({
    id("receiveQR")
  })
}