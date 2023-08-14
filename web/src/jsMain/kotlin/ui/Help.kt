package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*

@Composable
fun Help() {
  H1 { Text("MiniPay") }
  H3 { Text("About") }
  Text("MiniPay is a payments app on Minima built to support:")
  Ul {
    Li { Text("Pay to an address by scanning a QR code or tapping NFC (in the companion Android app)") }
    Li { Text("Request a payment to your address by presenting a QR code or your phone’s NFC device (in the companion Android app)") }
    Li { Text("Establish a Layer 2 payment channel using Maxima, QR codes or NFC (in the companion Android app) for P2P messaging.") }
    Li { Text("Pay and request payments over an existing L2 channel") }
    Li { Text("Pay and request payments using NFC over existing L2 channel even while offline (in the companion Android app)") }
  }
  H3 { Text("Instructions") }
  Text("Work in progress...")
}