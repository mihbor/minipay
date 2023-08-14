package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Help() {
  Column(Modifier.padding(20.dp)) {
    Row { Text("MiniPay", fontSize = 24.sp, fontWeight = ExtraBold) }
    Row { Text("About", fontSize = 20.sp, fontWeight = Bold) }
    Column {
      Text("MiniPay is a payments app on Minima built to support:")
      ListItem { Text("Pay to an address by scanning a QR code or tapping NFC") }
      ListItem { Text("Request a payment to your address by presenting a QR code or your phone’s NFC device") }
      ListItem { Text("Establish a Layer 2 payment channel using Maxima, QR codes or NFC for P2P messaging.") }
      ListItem { Text("Pay and request payments over an existing L2 channel") }
      ListItem { Text("Pay and request payments using NFC over existing L2 channel even while offline") }
    }
    Row { Text("Instructions", fontSize = 20.sp, fontWeight = Bold) }
    Column {
      Text("Work in progress...")
    }
  }
}

@Composable @Preview
fun PreviewHelp() {
  MiniPayTheme {
    Help()
  }
}