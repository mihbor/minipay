package ltd.mbor.minipay.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Contact

@Composable
fun ContactActions(contact: Contact, selectContact: (Contact) -> Unit) {
  Button({ selectContact(contact) }/*, enabled = contact.sameChain*/) {
    Text("Create Channel")
//    Text(if (contact.sameChain) "Create Channel" else "Not same chain!")
  }
}