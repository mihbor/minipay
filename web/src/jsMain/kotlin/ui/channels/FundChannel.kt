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
import ltd.mbor.minipay.common.model.ChannelInvite
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
  invite: ChannelInvite,
  setInvite: (ChannelInvite) -> Unit
) {
  var myAmount by remember { mutableStateOf(ZERO) }
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
      window.confirm("Fund new channel with ${myAmount.toPlainString()} ${balances[invite.tokenId]?.tokenName ?: "[${invite.tokenId}]"}?")
    ) scope.launch {
      logic.fundChannel(myKeys, invite.keys, myAddress, invite.address, myAmount, invite.balance, invite.tokenId, timeLock) { event, newChannel ->
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
    TextInput(invite.keys.trigger) {
      onInput {
        setInvite(invite.copy(keys = invite.keys.copy(trigger = it.value)))
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty update key:")
    TextInput(invite.keys.update) {
      onInput {
        setInvite(invite.copy(keys = invite.keys.copy(update = it.value)))
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty settlement key:")
    TextInput(invite.keys.settle) {
      onInput {
        setInvite(invite.copy(keys = invite.keys.copy(settle = it.value)))
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty address:")
    TextInput(invite.address) {
      onInput {
        setInvite(invite.copy(address = it.value))
      }
      style {
        width(550.px)
      }
    }
    Br()
    Text("Counterparty contribution to channel:")
    DecimalNumberInput(invite.balance, min = ZERO) {
      it?.let { setInvite(invite.copy(balance = it)) }
    }
    TokenSelect(invite.tokenId, balances, tokens, showBalances = false) {
      setInvite(invite.copy(tokenId = it))
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
  if (listOf(myKeys.trigger, myKeys.update, myKeys.settle, invite.keys.trigger, invite.keys.update, invite.keys.settle, invite.address).all(String::isNotEmpty)
    && fundingTxStatus.isEmpty()
  ) {
    DecimalNumberInput(myAmount, min = ZERO) {
      it?.let { myAmount = it }
    }
    TokenSelect(invite.tokenId, balances, disabled = true) {}
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
  if (showFundScanner) FundChannelQR(progressStep == 0, setInvite)
}