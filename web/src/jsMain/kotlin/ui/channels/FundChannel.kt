package ui.channels

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.browser.window
import kotlinx.coroutines.launch
import logic.eltooScriptAddress
import logic.multisigScriptAddress
import logic.multisigScriptBalances
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.FundChannelEvent
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.scope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import ui.DecimalNumberInput
import ui.TokenSelect

@Composable
fun FundChannel(
  myKeys: Channel.Keys,
  myAddress: String,
  balances: SnapshotStateMap<String, Balance>,
  tokens: SnapshotStateMap<String, Token>,
) {
  var myAmount by remember { mutableStateOf(ZERO) }
  var theirAmount by remember { mutableStateOf(ZERO) }
  var theirAddress by remember { mutableStateOf("") }
  var tokenId by remember { mutableStateOf("0x00") }
  
  var theirKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var timeLock by remember { mutableStateOf(10) }
  
  var fundingTxStatus by remember { mutableStateOf("") }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }

  var showFundScanner by remember { mutableStateOf(false) }
  var progressStep: Int by remember { mutableStateOf(0) }
  
  var channel by remember { mutableStateOf<Channel?>(null) }
  
  LaunchedEffect("fundChannel") {
    fundingTxStatus = ""
    triggerTxStatus = ""
    settlementTxStatus = ""
    multisigScriptAddress = ""
    eltooScriptAddress = ""
    multisigScriptBalances.clear()
  }

  fun fundChannel() {
    if (
      window.confirm("Fund new channel with ${myAmount.toPlainString()} ${balances[tokenId]?.tokenName ?: "[$tokenId]"}?")
    ) scope.launch {
      logic.fundChannel(myKeys, theirKeys, myAddress, theirAddress, myAmount, theirAmount, tokenId, timeLock) { event, newChannel ->
        progressStep++
        when (event) {
          FundChannelEvent.FUNDING_TX_CREATED -> fundingTxStatus = "Funding transaction created"
          FundChannelEvent.TRIGGER_TX_SIGNED -> triggerTxStatus = "Trigger transaction created and signed"
          FundChannelEvent.SETTLEMENT_TX_SIGNED -> settlementTxStatus = "Settlement transaction created and signed"
          FundChannelEvent.CHANNEL_PUBLISHED -> {
            triggerTxStatus += ", sent"
            settlementTxStatus += ", sent"
            channel = newChannel
            console.log("channelId", channel!!.id)
          }

          FundChannelEvent.SIGS_RECEIVED -> {
            triggerTxStatus += " and received back."
            settlementTxStatus += " and received back."
          }

          FundChannelEvent.CHANNEL_FUNDED -> fundingTxStatus += ", signed and posted!"
          FundChannelEvent.CHANNEL_UPDATED, FundChannelEvent.CHANNEL_UPDATED_ACKED -> {
            channel = newChannel
            progressStep--
          }

          else -> {}
        }
      }
    }
  }

  fun fundChannelQR() {
    showFundScanner = false
//    qrScanner?.stop()
    fundChannel()
  }

  Br()
  if (progressStep > 0) {
    Progress({
      attr("value", progressStep.toString())
      attr("max", 8.toString())
      style {
        width(500.px)
      }
    })
    Br()
  }
  if (fundingTxStatus.isEmpty()) {
    Text("Counterparty trigger key:")
    TextInput(theirKeys.trigger) {
      onInput {
        theirKeys = theirKeys.copy(trigger = it.value)
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty update key:")
    TextInput(theirKeys.update) {
      onInput {
        theirKeys = theirKeys.copy(update = it.value)
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty settlement key:")
    TextInput(theirKeys.settle) {
      onInput {
        theirKeys = theirKeys.copy(settle = it.value)
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty address:")
    TextInput(theirAddress) {
      onInput {
        theirAddress = it.value
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty contribution to channel:")
    DecimalNumberInput(theirAmount, min = ZERO) {
      it?.let { theirAmount = it }
    }
    TokenSelect(tokenId, balances, tokens, showBalances = false) {
      tokenId = it
    }
    Br()
  }
  fundingTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  triggerTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  channel?.let {
    ChannelView(it, balances) {
      channel = it
    }
  }
  if (listOf(myKeys.trigger, myKeys.update, myKeys.settle, theirKeys.trigger, theirKeys.update, theirKeys.settle, theirAddress).all(String::isNotEmpty)
    && fundingTxStatus.isEmpty()
  ) {
    DecimalNumberInput(myAmount, min = ZERO) {
      it?.let { myAmount = it }
    }
    TokenSelect(tokenId, balances, disabled = true) {
      tokenId = it
    }
    Text("Update only time lock (block diff)")
    NumberInput(timeLock, min = 0) {
      onInput {
        timeLock = it.value!!.toInt()
      }
    }
    Button({
      if (myAmount <= 0) disabled()
      onClick {
        fundChannelQR()
      }
    }) {
      Text("Initiate!")
    }
  }
  if (showFundScanner) FundChannelQR(progressStep == 0) { keys, token, amount, address ->
    theirKeys = keys
    tokenId = token
    theirAmount = amount
    theirAddress = address
  }
}