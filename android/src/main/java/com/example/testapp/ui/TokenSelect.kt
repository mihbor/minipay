package com.example.testapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TokenSelect(
  tokenId: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>? = null,
  enabled: Boolean = true,
  setTokenId: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val unifiedBalances = (tokens?.asBalances(balances) ?: balances)
  ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
    OutlinedTextField(
      value = unifiedBalances[tokenId]?.let { (it.tokenName ?: "Minima") + " [${it.sendable.toPlainString().take(12)}]" } ?: "",
      { },
      modifier = Modifier.fillMaxWidth(),
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
          Text(it.tokenName ?: "Minima")
          Text(" [${it.sendable.toPlainString()}]")
        }
      }
    }
  }
}

fun Map<String, Token>.asBalances(balances: Map<String, Balance>) = mapValues{
  balances[it.key] ?: it.value.toEmptyBalance()
}

fun Token.toEmptyBalance() = Balance(tokenId, _name, ZERO, ZERO, ZERO, "0")
