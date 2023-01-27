package ui

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun CopyToClipboard(value: String) {
  Button({
    if (value.isBlank()) disabled()
    onClick {
      window.navigator.clipboard.writeText(value)
    }
  }) {
    Text("⎘")
  }
}