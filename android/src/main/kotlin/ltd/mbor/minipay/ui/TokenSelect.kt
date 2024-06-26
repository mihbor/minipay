package ltd.mbor.minipay.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TokenSelect(
  tokenId: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>? = null,
  enabled: Boolean = true,
  showBalances: Boolean = true,
  setTokenId: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val unifiedBalances = (tokens?.asBalances(balances) ?: balances)
  ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
    OutlinedTextField(
      value = unifiedBalances[tokenId]?.let {
        buildString {
          append(it.tokenName ?: "[$tokenId]")
          if (showBalances) append(" [${it.sendable.toPlainString().take(12)}]")
        }
      } ?: "",
      { },
      readOnly = true,
      enabled = enabled,
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(
          expanded = expanded
        )
      }
    )
    ExposedDropdownMenu(expanded, { expanded = false }) {
      unifiedBalances.values.sortedByDescending { it.sendable }.forEach {
        DropdownMenuItem(enabled = enabled, onClick = {
          setTokenId(it.tokenId)
          expanded = false
        }) {
          Text(it.tokenName ?: "[$tokenId]")
          if (showBalances) Text(" [${it.sendable.toPlainString()}]")
        }
      }
    }
  }
}

fun Map<String, Token>.asBalances(balances: Map<String, Balance>) = mapValues{
  balances[it.key] ?: it.value.toEmptyBalance()
}

fun Token.toEmptyBalance() = Balance(tokenId, _name, ZERO, ZERO, ZERO, ZERO, "0")

@Composable
@Preview
fun PreviewTokenSelect() {
  MiniPayTheme {
    TokenSelect(previewBalances.values.first().tokenId, previewBalances, tokens = previewTokens, enabled = true) {}
  }
}
