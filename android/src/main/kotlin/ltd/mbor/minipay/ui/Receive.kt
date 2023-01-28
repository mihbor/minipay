package ltd.mbor.minipay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Receive(
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  address: String,
  setAddress: (String) -> Unit,
  tokenId: String,
  setTokenId: (String) -> Unit,
  amount: BigDecimal,
  setAmount: (BigDecimal?) -> Unit
) {
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

@Composable @Preview(showBackground = true)
fun PreviewReceive() {
  MiniPayTheme {
    Column {
      Receive(previewBalances, previewTokens, "address", {}, "0x00", {}, BigDecimal.ZERO) {}
    }
  }
}