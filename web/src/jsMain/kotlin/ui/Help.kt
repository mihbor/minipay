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
  H4 { Text("Receive") }
  Text("Here you can see and copy one of you Minima addresses to provide to someone who you want to send you tokens.")
  Br()
  Text("You can also see a QA code which can be scanned from another device that will populate the send screen" +
    " with your address, token and amount (if selected)")
  Br()
  Text("The 'Receive on NFC' link will open the companion Android app (if installed) in the receive mode, which emits the above data over NFC" +
    " allowing another Android device with MiniPay installed to open the pre-populated 'Send' screen" +
    " by simply bringing the devices into contactless (NFC) range.")
  H4{ Text("Send") }
  Text("Work in progress...")
}