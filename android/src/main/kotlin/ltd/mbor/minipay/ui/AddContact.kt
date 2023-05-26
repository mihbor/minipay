package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.addContact
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun AddContact() {
  var show by remember { mutableStateOf(false) }
  var contact by remember { mutableStateOf("") }
  var inProgress by remember { mutableStateOf(false) }
  if (show) {
    Column {
      OutlinedTextField(contact, { contact = it }, Modifier.fillMaxWidth())
      Row {
        Button(enabled = !inProgress && contact.isNotBlank(), onClick = {
          inProgress = true
          scope.launch {
            MDS.addContact(contact)?.let {
              contact = ""
            }
            inProgress = false
            show = false
          }
        }) {
          Text("Add")
        }
        Button({ show = false }) {
          Text("Cancel")
        }
      }
    }
  } else {
    Button({ show = true }) {
      Text("Add new contact")
    }
  }
}

@Composable @Preview
fun PreviewAddContact() {
  MiniPayTheme {
    AddContact()
  }
}