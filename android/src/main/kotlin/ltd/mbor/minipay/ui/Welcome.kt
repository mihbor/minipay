package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Welcome(inited: Boolean, setView: (String) -> Unit) {
  Text("Welcome to MiniPay", modifier = Modifier.padding(20.dp), fontSize = 20.sp)
  ProvideTextStyle(value = TextStyle(fontSize = 16.sp)) {
    Column(Modifier.padding(20.dp)) {
      if (inited) Text("Please select an option from the menu ☰")
      else {
        Row {
          Text("Please go to ")
          ClickableText(
            AnnotatedString("Settings"),
            style = TextStyle(textDecoration = Underline, color = Color.Blue, fontSize = 17.sp)
          ) { setView("Settings") }
        }
        Text("and update the UID")
        Text("to initialize your Minima connection")
      }
    }
  }
}

@Composable @Preview
fun PreviewWelcomeInited() {
  MiniPayTheme {
    Column {
      Welcome(true) { }
    }
  }
}

@Composable @Preview
fun PreviewWelcomeNotInited() {
  MiniPayTheme {
    Column {
      Welcome(false) { }
    }
  }
}