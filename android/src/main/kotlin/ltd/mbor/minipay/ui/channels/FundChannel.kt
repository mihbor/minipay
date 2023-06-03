package ltd.mbor.minipay.ui.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.log
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.logic.eltooScriptAddress
import ltd.mbor.minipay.logic.fundChannel
import ltd.mbor.minipay.logic.multisigScriptAddress
import ltd.mbor.minipay.logic.multisigScriptBalances
import ltd.mbor.minipay.ui.DecimalNumberField
import ltd.mbor.minipay.ui.TokenSelect
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewInvite
import ltd.mbor.minipay.ui.preview.previewKeys
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun FundChannel(
  myKeys: Channel.Keys,
  myAddress: String,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  activity: MainActivity?,
  invite: ChannelInvite,
  setInvite: (ChannelInvite) -> Unit
) {
  var myAmount by remember { mutableStateOf(ZERO) }

  var timeLock by remember { mutableStateOf(10) }

  var fundingTxStatus by remember { mutableStateOf("") }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }

  var showFundScannerOption by remember { mutableStateOf(true) }
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
  if (progressStep > 0) {
    LinearProgressIndicator(
      progressStep/8.0f,
      Modifier.fillMaxWidth(),
    )
    Text("Keep this screen open until you see channel balance. This may take a few minutes")
  }

  ProvideTextStyle(value = TextStyle(fontSize = 12.sp)) {
    if (fundingTxStatus.isEmpty()) {
      Text("Counterparty trigger key:")
      OutlinedTextField(
        invite.keys.trigger,
        { setInvite(invite.copy(keys = invite.keys.copy(trigger = it))) },
        Modifier.fillMaxWidth()
      )
      Text("Counterparty update key:")
      OutlinedTextField(
        invite.keys.update,
        { setInvite(invite.copy(keys = invite.keys.copy(update = it))) },
        Modifier.fillMaxWidth()
      )
      Text("Counterparty settlement key:")
      OutlinedTextField(
        invite.keys.settle,
        { setInvite(invite.copy(keys = invite.keys.copy(settle = it))) },
        Modifier.fillMaxWidth()
      )
      Text("Counterparty address:")
      OutlinedTextField(
        invite.address,
        { setInvite(invite.copy(address = it)) },
        Modifier.fillMaxWidth()
      )
      Text("Counterparty contribution to channel:")
      Row {
        DecimalNumberField(invite.balance, min = ZERO, modifier = Modifier.fillMaxWidth(0.5f)) {
          it?.let { setInvite(invite.copy(balance = it)) }
        }
        TokenSelect(tokenId = invite.tokenId, balances = balances, showBalances = false, tokens = tokens) {
          setInvite(invite.copy(tokenId = it))
        }
      }
    }
    fundingTxStatus.takeUnless { it.isEmpty() }?.let {
      Text(it)
    }
    triggerTxStatus.takeUnless { it.isEmpty() }?.let {
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
    if (listOf(myKeys.trigger, myKeys.update, myKeys.settle, invite.keys.trigger, invite.keys.update, invite.keys.settle, invite.address).all(String::isNotEmpty)
      && fundingTxStatus.isEmpty()
    ) {
      Text("My contribution to channel:")
      Row {
        DecimalNumberField(myAmount, min = ZERO, modifier = Modifier.fillMaxWidth(0.5f)) {
          it?.let { myAmount = it }
        }
        TokenSelect(invite.tokenId, balances, enabled = false) {}
      }
      Row {
        Text("Update only time lock (block diff)")
        DecimalNumberField(timeLock.toBigDecimal(), min = ZERO) {
          timeLock = it!!.intValue()
        }
      }
      Button(
        enabled = showFundScannerOption,
        onClick = {
          showFundScannerOption = false
          scope.launch {
            fundChannel(invite, myKeys, myAddress,  myAmount, timeLock) { event, newChannel ->
              progressStep++
              when (event) {
                FUNDING_TX_CREATED -> fundingTxStatus = "Funding transaction created"
                TRIGGER_TX_SIGNED -> triggerTxStatus = "Trigger transaction created and signed"
                SETTLEMENT_TX_SIGNED -> settlementTxStatus = "Settlement transaction created and signed"
                CHANNEL_PUBLISHED -> {
                  triggerTxStatus += ", sent"
                  settlementTxStatus += ", sent"
                  channel = newChannel
                  log("channelId: ${channel!!.id}")
                }
                SIGS_RECEIVED -> {
                  triggerTxStatus += " and received back."
                  settlementTxStatus += " and received back."
                }
                CHANNEL_FUNDED -> fundingTxStatus += ", signed and posted!"
                CHANNEL_UPDATED, CHANNEL_UPDATED_ACKED -> {
                  channel = newChannel
                  progressStep--
                }
                else -> {}
              }
            }
          }
        }
      ) {
        Text("Initiate!")
      }
    }
  }
  if (showFundScannerOption) FundChannelQR(progressStep == 0, setInvite)
}

@Composable @Preview(showBackground = true)
fun PreviewFundChannel() {
  MiniPayTheme {
    Column {
      FundChannel(previewKeys, "abc", previewBalances, previewTokens, null, previewInvite, {})
    }
  }
}
