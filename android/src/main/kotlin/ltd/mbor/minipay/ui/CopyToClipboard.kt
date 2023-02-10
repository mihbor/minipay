package ltd.mbor.minipay.ui

import android.widget.Toast
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CopyToClipboard(value: String) {
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  Button(onClick = {
    clipboardManager.setText(AnnotatedString(value))
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
  }, enabled = value.isNotBlank()) {
    Text("⎘")
  }
}