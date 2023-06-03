package ui

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.*

@Composable
fun Contacts(selectContact: (Contact) -> Unit) {
  val contacts = remember { mutableStateListOf<Contact>() }
  suspend fun refreshContacts() {
    val newContacts = MDS.getContacts()
    contacts.clear()
    contacts.addAll(newContacts)
  }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }
  Button({
    onClick {
      scope.launch {
        refreshContacts()
      }
    }
  }) {
    Text("Refresh")
  }
  AddContact{
    refreshContacts()
  }
  if (contacts.isEmpty()) Text("No contacts yet")
  else Table({
    style {
      textAlign("right")
      property("border-collapse", "collapse")
    }
  }) {
    Thead {
      Tr {
        Th { Text("ID") }
        Th { Text("Name") }
        Th { Text("Channel Actions") }
      }
    }
    Tbody {
      contacts.forEach { contact ->
        key(contact.id) {
          Tr({
            style { property("border-top", "1px solid black") }
          }) {
            Td { Text(contact.id.toString()) }
            Td { Text(contact.extraData.name) }
            Td {
              ContactActions(contact, selectContact)
            }
          }
        }
      }
    }
  }
}