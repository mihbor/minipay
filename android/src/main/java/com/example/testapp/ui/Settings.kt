package com.example.testapp.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun Settings(uid: String, setUid: (String) -> Unit) {
  var uidInput by remember { mutableStateOf(uid) }
  Row{
    Text("MiniDApp UID:")
  }
  Row{
    OutlinedTextField(value = uidInput,
      modifier = Modifier.fillMaxWidth(),
      //        textStyle = TextStyle(fontSize = (16.sp)),
      onValueChange = { uidInput = it }
    )
  }
  Row{
    Button(onClick = {
      setUid(uidInput)
    }){
      Text("Update")
    }
  }
}