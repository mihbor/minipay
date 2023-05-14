package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.ChannelInviteReceived
import ltd.mbor.minipay.common.model.PaymentRequestReceived
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.model.Transport.NFC
import ltd.mbor.minipay.ui.preview.previewEvents
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelEvents(
  events: MutableList<ChannelEvent>,
  tokens: Map<String, Token>,
  activity: MainActivity?
) {
  LazyColumn {
    items(events) {
      when {
        it is PaymentRequestReceived -> {
          if (it.transport == NFC) {
            ChannelRequestReceivedNfc(
              it.channel,
              it.updateTxId,
              it.settleTxId,
              it.sequenceNumber,
              it.channelBalance,
              tokens,
              activity
            ) {
              events -= it
            }
          } else {
            ChannelRequestReceived(
              it.channel,
              it.updateTxId,
              it.settleTxId,
              it.sequenceNumber,
              it.channelBalance,
              tokens,
            ) {
              events -= it
            }
          }
        }
        it is PaymentRequestSent -> {
          if (it.transport == NFC) {
            ChannelRequestSentNfc({ activity?.enableReaderMode() }) {
              events.remove(it)
            }
          } else {
            ChannelRequestSent {
              events -= it
            }
          }
        }
        it is ChannelInviteReceived -> {
          InviteReceived(it.invite) {
            events -= it
          }
        }
      }
      Divider()
    }
  }
}

@Composable @Preview
fun PreviewChannelEvents() {
  MiniPayTheme {
    ChannelEvents(previewEvents, previewTokens, null)
  }
}