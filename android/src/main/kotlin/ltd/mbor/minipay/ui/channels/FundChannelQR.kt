package ltd.mbor.minipay.ui.channels

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import ltd.mbor.minipay.TAG
import ltd.mbor.minipay.common.model.Channel

@Composable
fun FundChannelQR(noProgressYet: Boolean, onScan: (Channel.Keys, String, BigDecimal, String) -> Unit) {
  val scanLauncher = rememberLauncherForActivityResult(
    contract = ScanContract(),
    onResult = { result ->
      Log.i(TAG, "scanned code: ${result.contents}")
      result.contents?.split(';')?.apply {
        onScan(Channel.Keys(this[0], this[1], this[2]), this[3], this[4].toBigDecimal(), this[5])
      }
    }
  )
  if (noProgressYet) {
    Button(onClick = {
      scanLauncher.launch(ScanOptions().apply {
        setOrientationLocked(false)
        setPrompt("")
        setBeepEnabled(false)
      })
    }) {
      Text(text = "Scan QR")
    }
  }
}