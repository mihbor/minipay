package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img

@Composable
fun TokenIcon(url: String) {
  Img(url) {
    style {
      width(20.px)
      height(20.px)
      padding(4.px)
      property("vertical-align", "middle")
    }
  }
}

@Composable
fun TokenIcon(tokenId: String, tokens: Map<String, Token>) {
  TokenIcon(tokens[tokenId]?.url?.takeIf { it.isNotBlank() }
    ?: if (tokenId == "0x00") "minima.svg" else "coins.svg")
}

@Composable
fun TokenIcon(tokenId: String, balances: Map<String, Balance>) {
  TokenIcon(balances[tokenId]?.tokenUrl?.takeIf { it.isNotBlank() }
    ?: if (tokenId == "0x00") "minima.svg" else "coins.svg")
}