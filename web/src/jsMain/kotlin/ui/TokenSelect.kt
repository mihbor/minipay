package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun TokenSelect(
  tokenId: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>? = null,
  disabled: Boolean = false,
  setTokenId: (String) -> Unit
) {
  val unifiedBalances = (tokens?.asBalances(balances) ?: balances)
  Select({
    if (disabled) disabled()
    onChange {
      setTokenId(it.value!!)
    }
  }) {
    unifiedBalances.values.sortedByDescending { it.sendable }.forEach { balance ->
      key(balance.tokenId) {
        Option(balance.tokenId, { if (balance.tokenId == tokenId) selected() }) {
          Text("${balance.tokenName ?: "Minima"} (${balance.sendable.toPlainString()})")
        }
      }
    }
  }
}

fun Map<String, Token>.asBalances(balances: Map<String, Balance>) = mapValues{
  balances[it.key] ?: it.value.toEmptyBalance()
}

fun Token.toEmptyBalance() = Balance(tokenId, _name, ZERO, ZERO, ZERO, "0")
