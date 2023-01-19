package ltd.mbor.minipay.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.logic.ChannelEvent
import ltd.mbor.minipay.logic.PaymentRequestReceived
import ltd.mbor.minipay.logic.PaymentRequestSent
import ltd.mbor.minipay.ui.preview.previewEvents
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun ChannelEvents(events: MutableList<ChannelEvent>, tokens: Map<String, Token>, activity: MainActivity?) {
  LazyColumn {
    items(events) {
      when {
        it is PaymentRequestReceived && it.isNfc -> ChannelRequestReceivedNfc(
          it.channel,
          it.updateTxId,
          it.settleTxId,
          it.sequenceNumber,
          it.channelBalance,
          tokens,
          activity
        ) {
          events.remove(it)
        }
        it is PaymentRequestReceived && !it.isNfc -> ChannelRequestReceived(
          it.channel,
          it.updateTxId,
          it.settleTxId,
          it.sequenceNumber,
          it.channelBalance,
          tokens,
        ) {
          events.remove(it)
        }
        it is PaymentRequestSent && it.isNfc -> ChannelRequestSentNfc({ activity?.enableReaderMode() }) {
          events.remove(it)
        }
        it is PaymentRequestSent && !it.isNfc -> ChannelRequestSent{
          events.remove(it)
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