package com.example.testapp.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.TAG
import com.example.testapp.scope
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.send

@Composable
fun Send(
  inited: Boolean,
  balances: Map<String, Balance>,
  address: String,
  setAddress: (String) -> Unit,
  tokenId: String,
  setTokenId: (String) -> Unit,
  amount: BigDecimal,
  setAmount: (BigDecimal?) -> Unit
) {
  if (inited) {
    val context = LocalContext.current
    OutlinedTextField(address, setAddress, enabled = true, modifier = Modifier.fillMaxWidth())
    TokenSelect(tokenId, balances, enabled = true, setTokenId = setTokenId)
    Row{
      DecimalNumberField(amount, enabled = true, setValue = setAmount)
      var sending by remember { mutableStateOf(false) }
      Button(
        enabled = !sending && address.isNotBlank() && amount > BigDecimal.ZERO && balances[tokenId]?.sendable?.let{ it >= amount } ?: false,
        onClick = {
          sending = true
          scope.launch {
            val success = MDS.send(address, amount, tokenId)
            sending = false
            Toast.makeText(context, "Sending result: $success", Toast.LENGTH_LONG).show()
            if (success) {
              setAmount(BigDecimal.ZERO)
            }
          }
        }
      ) {
        Text("Send!")
      }
    }
    Row{
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
}