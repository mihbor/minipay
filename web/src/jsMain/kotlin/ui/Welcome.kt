package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*

@Composable
fun Welcome(setView: (String) -> Unit) {
  H1 {
    Text("Welcome to MiniPay")
  }
  H2 {
    Text("Please select an option from the menu")
  }
  H3 {
    Text("You can also download a native Android app in: ")
    A(href="#", {
      onClick{
        setView("Settings")
      }
    }) { Text("Settings") }
  }
}