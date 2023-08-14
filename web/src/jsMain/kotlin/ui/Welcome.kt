package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*

@Composable
fun Welcome(setView: (String) -> Unit) {
  H1 {
    Text("Welcome to MiniPay")
  }
  H2 {
    Text("A Minima payments app with L2 channel support and NFC (in the companion Android app)")
  }
  H3 {
    Text("Read more about it in: ")
    A(href="#", {
      onClick{
        setView("Help")
      }
    }) { Text("☰Help") }
  }
  H2 {
    Text("Please select an option from the corner menu ☰")
  }
  H3 {
    Text("You can also download a native Android app in: ")
    A(href="#", {
      onClick{
        setView("Settings")
      }
    }) { Text("☰Settings") }
  }
}