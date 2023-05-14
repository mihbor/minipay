package ltd.mbor.minipay.ui.channels

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import ltd.mbor.minipay.TAG
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite

@Composable
fun FundChannelQR(noProgressYet: Boolean, onScan: (ChannelInvite) -> Unit) {
  val scanLauncher = rememberLauncherForActivityResult(
    contract = ScanContract(),
    onResult = { result ->
      Log.i(TAG, "scanned code: ${result.contents}")
      result.contents?.split(';')?.apply {
        onScan(ChannelInvite(keys = Channel.Keys(this[0], this[1], this[2]), tokenId = this[3], balance = this[4].toBigDecimal(), address = this[5]))
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