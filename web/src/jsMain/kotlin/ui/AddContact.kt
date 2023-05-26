package ui

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.addContact
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea

@Composable
fun AddContact() {
  var show by remember { mutableStateOf(false) }
  var contact by remember { mutableStateOf("") }
  var inProgress by remember { mutableStateOf(false) }
  if (show) {
    TextArea(contact) {
      onInput {
        contact = it.value
      }
      style {
        width(500.px)
        height(80.px)
      }
    }
    Button({
      if (inProgress || contact.isBlank()) disabled()
      onClick {
        inProgress = true
        scope.launch {
          MDS.addContact(contact)?.let{
            contact = ""
          }
          inProgress = false
          show = false
        }
      }
    }) {
      Text("Add")
    }
    Button({
      onClick {
        show = false
      }
    }) {
      Text("Cancel")
    }
  } else {
    Button({
      onClick {
        show = true
      }
    }) {
      Text("Add new contact")
    }
  }
}