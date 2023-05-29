package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.ui.preview.previewContacts
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun Contacts(selectContact: (Contact) -> Unit) {
  val contacts = remember { mutableStateListOf<Contact>() }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }
  ContactsTable(contacts, selectContact) {
    scope.launch {
      val newContacts = MDS.getContacts()
      contacts.clear()
      contacts.addAll(newContacts)
    }
  }
}

@Composable
fun ContactsTable(contacts: List<Contact>, selectContact: (Contact) -> Unit, refresh: () -> Unit) {
  Column{
    Row {
      RefreshContacts(refresh)
      Spacer(Modifier.weight(1f))
      AddContact()
    }
    if (contacts.isEmpty()) Text("No contacts yet")
    else Row {
      Text("ID", Modifier.width(30.dp))
      Text("Name")
      Spacer(Modifier.weight(1f))
      Text("Channel Actions", Modifier.width(140.dp))
    }
    Divider()
    contacts.forEach { contact ->
      key(contact.id) {
        Row {
          Text(contact.id.toString(), Modifier.width(30.dp))
          Text(contact.extraData.name)
          Spacer(Modifier.weight(1f))
          ContactActions(contact, selectContact)
        }
        Divider()
      }
    }
  }
}

@Composable
fun RefreshContacts(refresh: () -> Unit) {
  Button(refresh) {
    Text("Refresh")
  }
}

@Composable @Preview
fun PreviewContactsTable() {
  MiniPayTheme {
    ContactsTable(previewContacts, {}) {}
  }
}

@Composable @Preview
fun PreviewEmptyContactsTable() {
  MiniPayTheme {
    ContactsTable(emptyList(), {}) {}
  }
}
