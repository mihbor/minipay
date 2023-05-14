package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.*

@Composable
fun Contacts() {
  val contacts = remember { mutableStateListOf<Contact>() }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }

  Table({
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
        Tr({
          style { property("border-top", "1px solid black") }
        }) {
          Td{ Text(contact.id.toString()) }
          Td{ Text(contact.extraData.name) }
          Td{
            ContactActions(contact)
          }
        }
      }
    }
  }
}