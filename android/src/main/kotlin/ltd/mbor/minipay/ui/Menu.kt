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
  if (showNavMenu) {
    Box(modifier = Modifier
      .fillMaxSize()
      .wrapContentSize(Alignment.TopStart)) {
      DropdownMenu(
        expanded = showNavMenu,
        onDismissRequest = { setShowNavMenu(false) }
      ) {
        DropdownMenuItem(onClick = {
          setView("Receive")
          startEmitting()
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Receive")
        }
        DropdownMenuItem(onClick = {
          setView("Send")
          stopEmitting()
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Send")
        }
        Divider()
        DropdownMenuItem(onClick = {
          setView("Create channel")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Create Channel")
        }
        DropdownMenuItem(onClick = {
          setView("Channels")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Channel Listing")
        }
        DropdownMenuItem(onClick = {
          setView("Channel events")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Channel Events")
        }
        Divider()
        DropdownMenuItem(onClick = {
          setView("Settings")
          setShowNavMenu(false)
        }) {
          Text("Settings")
        }
      }
    }
  }
}