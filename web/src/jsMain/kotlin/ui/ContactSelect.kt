package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getContacts
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun ContactSelect(contact: Contact?, setContact: (Contact?) -> Unit) {
  val contacts = remember { mutableStateListOf<Contact>() }
  LaunchedEffect("contacts") {
    contacts.addAll(MDS.getContacts())
  }
  Select({
    onChange {
      setContact(it.value?.toIntOrNull()?.let { contactId -> contacts.find { it.id == contactId } })
    }
  }) {
    Option("") {
      Text("Please select:")
    }
    contacts.sortedBy { it.extraData.name }.forEach {
      Option(it.id.toString(), {
        if (it.id == contact?.id) selected()
      }) {
        Text(it.extraData.name)
      }
    }
  }
}
