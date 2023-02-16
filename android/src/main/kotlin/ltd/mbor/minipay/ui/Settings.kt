package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Settings(prefs: Prefs, setPrefs: (Prefs) -> Unit) {
  var prefsInput by remember { mutableStateOf(prefs) }
  Row{
    Text("MiniDApp UID:")
  }
  Row{
    OutlinedTextField(
      value = prefsInput.uid,
      modifier = Modifier.fillMaxWidth(),
      onValueChange = { prefsInput = prefsInput.copy(uid = it) }
    )
  }
  Row(verticalAlignment = Alignment.CenterVertically) {
    Spacer(Modifier.weight(1f))
    Text("Host: ")
    OutlinedTextField(
      value = prefsInput.host,
      onValueChange = { prefsInput = prefsInput.copy(host = it) }
    )
  }
  Row(verticalAlignment = Alignment.CenterVertically) {
    Spacer(Modifier.weight(1f))
    Text("Port: ")
    OutlinedTextField(
      value = prefsInput.port.toString(),
      onValueChange = {
        it.toIntOrNull()?.takeIf { it in 1..65535 }?.let {
          prefsInput = prefsInput.copy(port = it)
        }
      }
    )
  }
  Row{
    Spacer(Modifier.weight(1f))
    Button(onClick = {
      setPrefs(prefsInput)
    }){
      Text("Update")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettings() {
  MiniPayTheme {
    Column {
      Settings(Prefs("", "localhost", 9004)) {}
    }
  }
}