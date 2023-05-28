package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.MaximaInfo
import ltd.mbor.minimak.getMaximaInfo
import ltd.mbor.minimak.setMaximaName
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.inited
import ltd.mbor.minipay.ui.preview.previewMaximaInfo
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Settings(prefs: Prefs, setPrefs: (Prefs) -> Unit) {
  var prefsInput by remember { mutableStateOf(prefs) }
  LazyColumn {
    item {
      Row {
        Text("MiniDApp UID:")
      }
      Row {
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
      Row {
        Spacer(Modifier.weight(1f))
        Button(onClick = {
          setPrefs(prefsInput)
        }) {
          Text("Update")
        }
      }
      if (inited) {
        var maximaInfo by remember { mutableStateOf<MaximaInfo?>(null) }
        LaunchedEffect("maxima") {
          maximaInfo = MDS.getMaximaInfo()
        }
        maximaInfo?.let { MaximaSettings(it) { maximaInfo = MDS.getMaximaInfo() } }
      }
    }
  }
}

@Composable
fun MaximaSettings(maximaInfo: MaximaInfo, refresh: suspend () -> Unit) {
  var maximaName by remember { mutableStateOf(maximaInfo.name) }
    Row{
      Text("My maxima contact:")
    }
    Row{
      OutlinedTextField(maximaInfo.contact, {}, Modifier.fillMaxWidth(), enabled = false)
    }
    Row{
      CopyToClipboard(maximaInfo.contact)
    }
    Row{
      Text("My maxima name:")
    }
    Row{
      OutlinedTextField(maximaName, { maximaName = it }, Modifier.weight(1f))
      Button(
        enabled = maximaName != maximaInfo.name,
        onClick = {
          scope.launch {
            MDS.setMaximaName(maximaName)
            refresh()
          }
        }
      ) {
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

@Preview(showBackground = true)
@Composable
fun PreviewMaximaSettings() {
  MiniPayTheme {
    Column {
      MaximaSettings(previewMaximaInfo) {}
    }
  }
}