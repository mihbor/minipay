package ui

import androidx.compose.runtime.Composable
import logic.ChannelEvent
import logic.PaymentRequestReceived
import logic.PaymentRequestSent
import ltd.mbor.minimak.Token
import org.jetbrains.compose.web.dom.Div

@Composable
fun ChannelEvents(
  events: MutableList<ChannelEvent>,
  tokens: Map<String, Token>,
) {
  events.reversed().forEach {
    Div({ classes(StyleSheets.container) }) {
      when (it) {
        is PaymentRequestReceived -> ChannelRequestReceived(
          it.channel,
          it.updateTxId,
          it.settleTxId,
          it.sequenceNumber,
          it.channelBalance,
          tokens
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