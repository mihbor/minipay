package ltd.mbor.minipay.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.send
import ltd.mbor.minipay.TAG
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Send(
  balances: Map<String, Balance>,
  toAddress: String,
  setAddress: (String) -> Unit,
  tokenId: String,
  setTokenId: (String) -> Unit,
  amount: BigDecimal,
  setAmount: (BigDecimal?) -> Unit
) {
  var isSending by remember { mutableStateOf(false) }
  var confirmSending by remember { mutableStateOf(false) }
  val context = LocalContext.current
  fun send() {
    isSending = true
    confirmSending = false
    scope.launch {
      val result = MDS.send(toAddress, amount, tokenId)
      isSending = false
      Toast.makeText(context, "Sending result: $result", Toast.LENGTH_LONG).show()
      if (result.isSuccessful) {
        setAmount(ZERO)
      }
    }
  }
  if (confirmSending) AlertDialog(
    onDismissRequest = { confirmSending = false },
    title = {
      Text("Sending confirmation")
    },
    text = {
      Text("Send ${amount.toPlainString()} ${balances[tokenId]?.tokenName ?: "[$tokenId]"} to $toAddress?")
    },
    confirmButton = {
      Button(::send) {
        Text("Send")
      }
    },
    dismissButton = {
      Button({ confirmSending = false }) {
        Text("Cancel")
      }
    }
  )
  OutlinedTextField(toAddress, setAddress, enabled = true, modifier = Modifier.fillMaxWidth())
  Row {
    TokenSelect(tokenId, balances, enabled = true, setTokenId = setTokenId)
    TokenIcon(tokenId, balances, size = 50)
  }
  Row {
    DecimalNumberField(amount, enabled = true, setValue = setAmount)
    Button(
      enabled = !isSending && toAddress.isNotBlank() && amount > ZERO && balances[tokenId]?.sendable?.let { it >= amount } ?: false,
      onClick = {
        confirmSending = true
      }
    ) {
      Text(if (isSending) "Sending" else "Send!")
    }
  }
  Row {
    val scanLauncher = rememberLauncherForActivityResult(
      contract = ScanContract(),
      onResult = { result ->
        Log.i(TAG, "scanned code: ${result.contents}")
        result.contents?.split(";")?.apply {
          setAddress(getOrNull(0) ?: "")
          setTokenId(getOrNull(1) ?: "")
          setAmount(getOrNull(2)?.toBigDecimal())
        }
      }
    )
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

@Composable @Preview(showBackground = true)
fun PreviewSend() {
  MiniPayTheme {
    Column {
      Send(previewBalances, "address", {}, "0x00", {}, ZERO) {}
    }
  }
}
