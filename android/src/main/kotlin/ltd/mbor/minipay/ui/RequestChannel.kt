package ltd.mbor.minipay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getAddress
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.newKeys
import ltd.mbor.minipay.logic.JoinChannelEvent.*
import ltd.mbor.minipay.logic.eltooScriptAddress
import ltd.mbor.minipay.logic.joinChannel
import ltd.mbor.minipay.logic.multisigScriptAddress
import ltd.mbor.minipay.logic.multisigScriptBalances
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun RequestChannel(
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  activity: MainActivity?,
) {
  var myAddress by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf(ZERO) }
  var tokenId by remember { mutableStateOf("0x00") }
  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }

  var showQR by remember { mutableStateOf(false) }
  var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var triggerTxStatus by remember { mutableStateOf("") }
  var updateTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }

  var progressStep: Int by remember { mutableStateOf(0) }

  var channel by remember { mutableStateOf<Channel?>(null) }

  LaunchedEffect("requestChannel") {
    MDS.newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    myAddress = MDS.getAddress().address
    triggerTxStatus = ""
    settlementTxStatus = ""
    multisigScriptAddress = ""
    eltooScriptAddress = ""
    multisigScriptBalances.clear()
  }

  fun requestChannel() {
    bitmap = encodeAsBitmap(channelKey(myKeys, tokenId) + ";" + amount.toPlainString() + ";" + myAddress).asImageBitmap()

    joinChannel(myAddress, myKeys, tokenId, amount) { event, newChannel ->
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
          channel = newChannel
          triggerTxStatus += " and sent back."
          settlementTxStatus += " and sent back."
        }
        CHANNEL_UPDATED -> {
          channel = newChannel
          updateTxStatus += "Update transaction received. "
        }
        CHANNEL_UPDATED_ACKED -> {
          channel = newChannel
          updateTxStatus += "Update transaction ack received. "
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
    ProvideTextStyle(value = TextStyle(fontSize = 12.sp)) {
      Row {
        Text("Trigger key: ${myKeys.trigger}", Modifier.fillMaxWidth(0.8f))
        CopyToClipboard(myKeys.trigger)
      }
      Row {
        Text("Update key: ${myKeys.update}", Modifier.fillMaxWidth(0.8f))
        CopyToClipboard(myKeys.update)
      }
      Row {
        Text("Settlement key: ${myKeys.settle}", Modifier.fillMaxWidth(0.8f))
        CopyToClipboard(myKeys.settle)
      }
      Row {
        Text("Address: $myAddress", Modifier.fillMaxWidth(0.8f))
        CopyToClipboard(myAddress)
      }
    }
    DecimalNumberField(amount, min = ZERO, enabled = !showQR) {
      it?.let { amount = it }
    }
    TokenSelect(tokenId, balances, tokens = tokens, enabled = !showQR) {
      tokenId = it
    }
    if (!showQR) Button(
      enabled = amount >= 0 && listOf(myKeys.trigger, myKeys.update, myKeys.settle).none{ it.isBlank() },
      onClick = {
        showQR = !showQR
        requestChannel()
      }
    ) {
      Text("Request channel")
    } else Button(
      onClick = {
        showQR = !showQR
      }
    ) {
      Text("Cancel")
    }
  }
  triggerTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
  }
  updateTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
  }
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
  }
  channel?.let {
    ChannelView(it, balances, activity) {
      channel = it
    }
  }
  if(showQR) bitmap?.let{ Image(bitmap = it, contentDescription = "Scan this QR code") }
}

@Composable @Preview(showBackground = true)
fun PreviewRequestChannel() {
  MiniPayTheme {
    Column {
      RequestChannel(previewBalances, previewTokens, null)
    }
  }
}
