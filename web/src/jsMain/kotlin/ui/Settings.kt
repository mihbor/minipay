package ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import ltd.mbor.minipay.common.Prefs
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.url.URLSearchParams

@Composable
fun Settings(prefs: Prefs, setPrefs: (Prefs) -> Unit) {
  var prefsInput by remember { mutableStateOf(prefs) }
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
  Br()
  TextInput(prefsInput.uid) {
    onInput {
      prefsInput = prefsInput.copy(uid = it.value)
    }
    style {
      width(500.px)
    }
  }
  CopyToClipboard(prefsInput.uid)
  Br()
  Text("Host:")
  Br()
  TextInput(prefsInput.host) {
    onInput {
      prefsInput = prefsInput.copy(host = it.value)
    }
    style {
      width(250.px)
    }
  }
  Br()
  Text("Port:")
  Br()
  NumberInput(prefsInput.port) {
    onInput {
      it.value?.toIntOrNull()?.takeIf { it in 1..65535 }?.let{ prefsInput = prefsInput.copy(port = it) }
    }
    style {
      width(80.px)
    }
  }
  Br()
  Br()
  Button({
    onClick {
      if (prefsInput.uid.isNotBlank()) {
        val searchParams = URLSearchParams(window.location.search)
        searchParams.set("uid", prefsInput.uid)
        val url = "${window.location.pathname}?${searchParams}"
        window.history.replaceState(url, "", url)
      }
      setPrefs(prefsInput)
    }
  }) {
    Text("Update")
  }
}

fun Number.toIntOrNull(): Int? {
  return try {
    toInt()
  } catch (e: Exception) {
    null
  }
}