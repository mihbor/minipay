package ltd.mbor.minipay.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.logic.PaymentRequestReceived
import ltd.mbor.minipay.logic.PaymentRequestSent
import ltd.mbor.minipay.logic.events

@Composable
fun ChannelEvents(tokens: Map<String, Token>, activity: MainActivity?) {
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
        it is PaymentRequestSent && it.isNfc -> ChannelRequestSentNfc({ activity?.enableReaderMode() }) {
          events.remove(it)
        }
      }
    }
  }
}