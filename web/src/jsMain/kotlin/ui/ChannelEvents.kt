package ui

import androidx.compose.runtime.Composable
import logic.PaymentRequestReceived
import logic.PaymentRequestSent
import logic.events
import org.jetbrains.compose.web.dom.Div

@Composable
fun ChannelEvents() {
  events.reversed().forEach {
    Div({ classes(StyleSheets.container) }) {
      when (it) {
        is PaymentRequestReceived -> ChannelRequestReceived(
          it.channel,
          it.updateTxId,
          it.settleTxId,
          it.sequenceNumber,
          it.channelBalance
        ) {
          events -= it
        }
      
        is PaymentRequestSent -> ChannelRequestSent {
          events -= it
        }
      }
    }
  }
}