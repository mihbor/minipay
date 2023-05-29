package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Contact
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ContactActions(contact: Contact, selectContact: (Contact) -> Unit) {
  Button({
    if (!contact.sameChain) disabled()
    onClick { selectContact(contact) }
  }) {
    Text(if (contact.sameChain) "Create Channel" else "Not same chain!")
  }
}