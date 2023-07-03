package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.logic.*
import ltd.mbor.minipay.ui.DecimalNumberField
import ltd.mbor.minipay.ui.TokenSelect
import ltd.mbor.minipay.ui.encodeAsBitmap
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewKeys
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun RequestChannel(
  myKeys: Channel.Keys,
  myAddress: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  maximaContact: Contact?,
  activity: MainActivity?,
) {
  var amount by remember { mutableStateOf(ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }

  var showQR by remember { mutableStateOf(false) }
  var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }

  var progressStep: Int by remember { mutableStateOf(0) }

  LaunchedEffect("requestChannel") {
    triggerTxStatus = ""
    settlementTxStatus = ""
    multisigScriptAddress = ""
    eltooScriptAddress = ""
    multisigScriptBalances.clear()
  }

  fun requestChannel() {
    bitmap = encodeAsBitmap(channelKey(myKeys, tokenId) + ";" + amount.toPlainString() + ";" + myAddress).asImageBitmap()

    requestChannel(ChannelInvite(tokenId, myAddress, amount, myKeys, null), maximaContact) { event, newChannel ->
      progressStep++
      when (event) {
        SIGS_RECEIVED -> {
          triggerTxStatus = "Trigger transaction received"
          settlementTxStatus = "Settlement transaction received"
          showQR = false
        }
        TRIGGER_TX_SIGNED -> triggerTxStatus += " and signed"
        SETTLEMENT_TX_SIGNED -> settlementTxStatus += " and signed"
        CHANNEL_PUBLISHED -> {
          requestedChannel = newChannel
          triggerTxStatus += " and sent back."
          settlementTxStatus += " and sent back."
        }
        CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED -> {
          requestedChannel = newChannel
          progressStep--
        }
        else -> {}
      }
    }
  }

  if (progressStep > 0) {
    LinearProgressIndicator(
      progressStep/6.0f,
      Modifier.fillMaxWidth(),
    )
    Text("Keep this screen open until you see channel balance. This may take a few minutes")
  }
  if (triggerTxStatus.isEmpty()) {
    DecimalNumberField(amount, min = ZERO, enabled = !showQR) {
      it?.let { amount = it }
    }
    TokenSelect(tokenId, balances, tokens = tokens, enabled = !showQR) {
      tokenId = it
    }
    if (!showQR) Button(
      enabled = amount >= 0 && listOf(myKeys.trigger, myKeys.update, myKeys.settle).none{ it.isBlank() },
      onClick = {
        showQR = maximaContact == null
        requestChannel()
      }
    ) {
      Text("Request channel")
    } else Button(
      onClick = {
        showQR = false
      }
    ) {
      Text("Cancel")
    }
  }
  triggerTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
  }
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
  }
  requestedChannel?.let { channel ->
    ChannelView(channels.find{ it.id == channel.id } ?: channel, balances, activity) {
      requestedChannel = it
    }
  }
  if(showQR) bitmap?.let{ Image(bitmap = it, contentDescription = "Scan this QR code") }
}

@Composable @Preview(showBackground = true)
fun PreviewRequestChannel() {
  MiniPayTheme {
    Column {
      RequestChannel(previewKeys, "abc", previewBalances, previewTokens, null, null)
    }
  }
}
