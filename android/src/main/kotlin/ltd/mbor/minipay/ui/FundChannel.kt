package ltd.mbor.minipay.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.log
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.TAG
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.newKeys
import ltd.mbor.minipay.logic.eltooScriptAddress
import ltd.mbor.minipay.logic.fundChannel
import ltd.mbor.minipay.logic.multisigScriptAddress
import ltd.mbor.minipay.logic.multisigScriptBalances
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun FundChannel(balances: Map<String, Balance>, tokens: Map<String, Token>, activity: MainActivity?) {

  var myAmount by remember { mutableStateOf(ZERO) }
  var theirAmount by remember { mutableStateOf(ZERO) }
  var theirAddress by remember { mutableStateOf("") }
  var tokenId by remember { mutableStateOf("0x00") }

  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var theirKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var timeLock by remember { mutableStateOf(10) }

  var fundingTxStatus by remember { mutableStateOf("") }
  var triggerTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }

  var showFundScanner by remember { mutableStateOf(true) }
  var progressStep: Int by remember { mutableStateOf(0) }

  var channel by remember { mutableStateOf<Channel?>(null) }

  LaunchedEffect("fundChannel") {
    MDS.newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
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

  LazyColumn {
    item {
      ProvideTextStyle(value = TextStyle(fontSize = 12.sp)) {
        if (fundingTxStatus.isEmpty()) {
          Text("My trigger key: ${myKeys.trigger}")
          Text("My update key: ${myKeys.update}")
          Text("My settlement key: ${myKeys.settle}")
          Text("Counterparty trigger key:")
          OutlinedTextField(
            theirKeys.trigger,
            { theirKeys = theirKeys.copy(trigger = it) },
            Modifier.fillMaxWidth()
          )
          Text("Counterparty update key:")
          OutlinedTextField(
            theirKeys.update,
            { theirKeys = theirKeys.copy(update = it) },
            Modifier.fillMaxWidth()
          )
          Text("Counterparty settlement key:")
          OutlinedTextField(
            theirKeys.settle,
            { theirKeys = theirKeys.copy(settle = it) },
            Modifier.fillMaxWidth()
          )
          Text("Counterparty address:")
          OutlinedTextField(
            theirAddress,
            { theirAddress = it },
            Modifier.fillMaxWidth()
          )
          Text("Counterparty contribution to channel:")
          Row {
            DecimalNumberField(theirAmount, min = ZERO, modifier = Modifier.fillMaxWidth(0.5f)) {
              it?.let { theirAmount = it }
            }
            TokenSelect(tokenId = tokenId, balances = balances, tokens = tokens) {
              tokenId = it
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
        if (listOf(myKeys.trigger, myKeys.update, myKeys.settle, theirKeys.trigger, theirKeys.update, theirKeys.settle, theirAddress).all(String::isNotEmpty)
          && fundingTxStatus.isEmpty()
        ) {
          Text("My contribution to channel:")
          Row {
            DecimalNumberField(myAmount, min = ZERO, modifier = Modifier.fillMaxWidth(0.5f)) {
              it?.let { myAmount = it }
            }
            TokenSelect(tokenId, balances, enabled = false) {
              tokenId = it
            }
          }
          Row {
            Text("Update only time lock (block diff)")
            DecimalNumberField(timeLock.toBigDecimal(), min = ZERO) {
              timeLock = it!!.intValue()
            }
          }
          Button(
            enabled = showFundScanner,
            onClick = {
              showFundScanner = false
              scope.launch {
                fundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock) { event, newChannel ->
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
      if (showFundScanner) {
        val scanLauncher = rememberLauncherForActivityResult(
          contract = ScanContract(),
          onResult = { result ->
            Log.i(TAG, "scanned code: ${result.contents}")
            result.contents?.split(';')?.apply {
              theirKeys = Channel.Keys(this[0], this[1], this[2])
              tokenId = this[3]
              theirAmount = this[4].toBigDecimal()
              theirAddress = this[5]
            }
          }
        )
        Button(onClick = {
          scanLauncher.launch(ScanOptions().apply {
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
          })
        }) {
          Text(text = "Scan QR")
        }
      }
    }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewFundChannel() {
  MiniPayTheme {
    Column {
      FundChannel(previewBalances, previewTokens, null)
    }
  }
}
