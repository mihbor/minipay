package com.example.testapp.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token

fun encodeAsBitmap(str: String): Bitmap {
  val writer = QRCodeWriter()
  val bitMatrix: BitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 800, 800)
  val w: Int = bitMatrix.getWidth()
  val h: Int = bitMatrix.getHeight()
  val pixels = IntArray(w * h)
  for (y in 0 until h) {
    for (x in 0 until w) {
      pixels[y * w + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
    }
  }
  val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
  bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
  return bitmap
}

@Composable
fun Receive(
  inited: Boolean,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  address: String,
  setAddress: (String) -> Unit,
  tokenId: String,
  setTokenId: (String) -> Unit,
  amount: BigDecimal,
  setAmount: (BigDecimal?) -> Unit
) {
  if (inited) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    if (address.isNotBlank()) {
      bitmap = encodeAsBitmap("$address;${amount.toPlainString()};$tokenId").asImageBitmap()
    }
    OutlinedTextField(address, setAddress, enabled = true, modifier = Modifier.fillMaxWidth())
    TokenSelect(tokenId, balances, tokens, true, setTokenId)
    Row{
      DecimalNumberField(amount, enabled = true, setValue = setAmount)
    }
    Row{
      bitmap?.let{ Image(bitmap = it, contentDescription = "Scan this QR code") }
    }
  }
}