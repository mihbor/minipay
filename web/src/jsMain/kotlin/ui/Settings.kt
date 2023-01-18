package ui

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import logic.getParams
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun Settings() {
  val uid = getParams("uid")
  A("minipay.apk") {
    Text("Download standalone Android app")
  }
  Br()
  Text("UID:")
  uid?.let {
    Br()
    Text(uid)
    Br()
    Button({
      if (uid.isBlank()) disabled()
      onClick {
        window.navigator.clipboard.writeText(uid)
      }
    }) {
      Text("⎘")
    }
  }
}