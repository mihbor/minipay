package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Contact
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun ContactActions(contact: Contact) {
  Button {
    Text("Create Channel")
  }
}