package ltd.mbor.minipay.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

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
