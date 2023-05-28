package ltd.mbor.minipay.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactSelect(contact: Contact?, setContact: (Contact?) -> Unit) {
  val contacts = remember { mutableStateListOf<Contact>() }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }
  var expanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
    OutlinedTextField(
      contact?.extraData?.name ?: "Please select:",
      { },
      readOnly = true,
      enabled = true,
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(
          expanded = expanded
        )
      }
    )
    ExposedDropdownMenu(expanded, { expanded = false }) {
      contacts.sortedBy { it.extraData.name }.forEach {
        DropdownMenuItem({
          setContact(it)
          expanded = false
        }) {
          Text(it.extraData.name)
        }
      }
    }
  }
}

@Composable @Preview
fun PreviewContactSelect() {
  MiniPayTheme {
    ContactSelect(null) {}
  }
}