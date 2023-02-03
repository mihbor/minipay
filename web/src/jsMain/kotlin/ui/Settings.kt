package ui

import androidx.compose.runtime.Composable
import logic.getParams
import org.jetbrains.compose.web.dom.*

@Composable
fun Settings() {
  val uid = getParams("uid")
  H2 {
    Text("Download the native ")
    A("minipay.apk") {
      Text("Android app")
    }
  }
  H3 {
    Text("Copy the UID from below and paste it in the Android app")
  }
  Text("UID:")
  uid?.let {
    Br()
    Text(uid)
    Br()
    CopyToClipboard(uid)
  }
}