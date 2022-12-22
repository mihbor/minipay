package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Token
import org.jetbrains.compose.web.dom.Text


@Composable
fun TokenName(tokenId: String, tokens: Map<String, Token>) {
  val token = tokens[tokenId]
  if (token != null) {
    TokenIcon(tokenId, tokens)
    Text(" ${token.name}")
  } else {
    Text(tokenId)
  }
}