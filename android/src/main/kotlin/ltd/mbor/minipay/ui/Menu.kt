package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Menu(inited: Boolean, showNavMenu: Boolean, setView: (String) -> Unit, startEmitting: () -> Unit, stopEmitting: () -> Unit, setShowNavMenu: (Boolean) -> Unit) {

  @Composable
  fun MenuItem(view: String, label: String = view, enabled: Boolean = inited, action: () -> Unit = {}) {
    DropdownMenuItem(onClick = {
      setView(view)
      setShowNavMenu(false)
      action()
    }, enabled = enabled) {
      Text(label)
    }
  }
  if (showNavMenu) {
    Box(modifier = Modifier
      .fillMaxSize()
      .wrapContentSize(Alignment.TopStart)) {
      DropdownMenu(
        expanded = showNavMenu,
        onDismissRequest = { setShowNavMenu(false) }
      ) {
        MenuItem("Receive") {
          startEmitting()
        }
        MenuItem("Send") {
          stopEmitting()
        }
        Divider()
        MenuItem("Create Channel")
        MenuItem("Channels", "Channel Listing")
        MenuItem("Channel Events")
        Divider()
        MenuItem("Contacts")
        Divider()
        MenuItem("Settings", enabled = true)
        Divider()
        MenuItem("Help", enabled = true)
      }
    }
  }
}