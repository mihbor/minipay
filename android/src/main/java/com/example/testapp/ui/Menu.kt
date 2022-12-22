package com.example.testapp.ui

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
          setView("receive")
          startEmitting()
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Receive")
        }
        DropdownMenuItem(onClick = {
          setView("send")
          stopEmitting()
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Send")
        }
        Divider()
        DropdownMenuItem(onClick = {
          setView("request-channel")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Request Channel")
        }
        DropdownMenuItem(onClick = {
          setView("fund-channel")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Fund Channel")
        }
        DropdownMenuItem(onClick = {
          setView("channels")
          setShowNavMenu(false)
        }, enabled = inited) {
          Text("Channel Listing")
        }
        Divider()
        DropdownMenuItem(onClick = {
          setView("settings")
          setShowNavMenu(false)
        }) {
          Text("Settings")
        }
      }
    }
  }
}