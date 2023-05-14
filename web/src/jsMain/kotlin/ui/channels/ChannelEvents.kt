package ui.channels

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.ChannelInviteReceived
import ltd.mbor.minipay.common.model.PaymentRequestReceived
import ltd.mbor.minipay.common.model.PaymentRequestSent
import org.jetbrains.compose.web.dom.Div
import ui.StyleSheets

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

        is ChannelInviteReceived -> InviteReceived{
          events -= it
        }
      }
    }
  }
}