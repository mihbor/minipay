package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import ltd.mbor.minipay.ui.preview.previewContacts
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Contacts() {
  val contacts = remember { mutableStateListOf<Contact>() }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }
  ContactsTable(contacts)
}

@Composable
fun ContactsTable(contacts: List<Contact>) {
  Column{
    AddContact()
    if (contacts.isEmpty()) Text("No contacts yet")
    else Row {
      Text("ID", Modifier.width(30.dp))
      Text("Name", Modifier.width(200.dp))
      Text("Channel Actions")
    }
    Divider()
    contacts.forEach { contact ->
      key(contact.id) {
        Row {
          Text(contact.id.toString(), Modifier.width(30.dp))
          Text(contact.extraData.name, Modifier.width(200.dp))
          ContactActions(contact)
        }
        Divider()
      }
    }
  }
}

@Composable @Preview
fun PreviewContactsTable() {
  MiniPayTheme {
    ContactsTable(previewContacts)
  }
}

@Composable @Preview
fun PreviewEmptyContactsTable() {
  MiniPayTheme {
    ContactsTable(emptyList())
  }
}
