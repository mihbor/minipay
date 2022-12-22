package ui

import Channel
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import externals.QrScanner
import kotlinx.browser.document
import kotlinx.coroutines.launch
import logic.FundChannelEvent.*
import logic.fundChannel
import logic.newKeys
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLVideoElement
import scope

@Composable
fun FundChannel(balances: SnapshotStateMap<String, Balance>, tokens: SnapshotStateMap<String, Token>) {
  var myAmount by remember { mutableStateOf(ZERO) }
  var theirAmount by remember { mutableStateOf(ZERO) }
  var theirAddress by remember { mutableStateOf("") }
  var tokenId by remember { mutableStateOf("0x00") }
  
  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var theirKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var timeLock by remember { mutableStateOf(10) }
  
  var fundingTxStatus by remember { mutableStateOf("") }
  var triggerTxStatus by remember { mutableStateOf("") }
  var updateTxStatus by remember { mutableStateOf("") }
  var settlementTxStatus by remember { mutableStateOf("") }
  
  var showFundScanner by remember { mutableStateOf(false) }
  var qrScanner: QrScanner? by remember { mutableStateOf(null) }
  var progressStep: Int by remember { mutableStateOf(0) }
  
  var channel by remember { mutableStateOf<Channel?>(null) }
  
  LaunchedEffect("fundChannel") {
    newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    fundingTxStatus = ""
    triggerTxStatus = ""
    settlementTxStatus = ""
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
    Text("My trigger key: ${myKeys.trigger}")
    Br()
    Text("My update key: ${myKeys.update}")
    Br()
    Text("My settlement key: ${myKeys.settle}")
    Br()
    Text("Counterparty trigger key:")
    TextInput(theirKeys.trigger) {
      onInput {
        theirKeys = theirKeys.copy(trigger = it.value)
      }
      style {
        width(500.px)
      }
    }
    Br()
    Text("Counterparty update key:")
    TextInput(theirKeys.update) {
      onInput {
        theirKeys = theirKeys.copy(update = it.value)
      }
      style {
        width(500.px)
      }
    }
    Br()
    Text("Counterparty settlement key:")
    TextInput(theirKeys.settle) {
      onInput {
        theirKeys = theirKeys.copy(settle = it.value)
      }
      style {
        width(500.px)
      }
    }
    Br()
    Text("Counterparty address:")
    TextInput(theirAddress) {
      onInput {
        theirAddress = it.value
      }
      style {
        width(300.px)
      }
    }
    Br()
    Text("Counterparty contribution to channel:")
    DecimalNumberInput(theirAmount, min = ZERO) {
      it?.let { theirAmount = it }
    }
    TokenSelect(tokenId, balances, tokens) {
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
  updateTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  settlementTxStatus.takeUnless { it.isEmpty() }?.let {
    Text(it)
    Br()
  }
  channel?.let {
    ChannelView(it) {
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
        showFundScanner = false
        qrScanner?.stop()
        scope.launch {
          fundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock) { event, newChannel ->
            progressStep++
            when(event) {
              FUNDING_TX_CREATED -> fundingTxStatus = "Funding transaction created"
              TRIGGER_TX_SIGNED -> triggerTxStatus = "Trigger transaction created and signed"
              SETTLEMENT_TX_SIGNED -> settlementTxStatus = "Settlement transaction created and signed"
              CHANNEL_PUBLISHED -> {
                triggerTxStatus += ", sent"
                settlementTxStatus += ", sent"
                channel = newChannel
                console.log("channelId", channel!!.id)
              }
              SIGS_RECEIVED -> {
                triggerTxStatus += " and received back."
                settlementTxStatus += " and received back."
              }
              CHANNEL_FUNDED -> fundingTxStatus += ", signed and posted!"
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
      }
    }) {
      Text("Initiate!")
    }
  }
  Br()
  Button({
    onClick {
      showFundScanner = !showFundScanner
    }
    style {
      if (showFundScanner) border(style = LineStyle.Inset)
    }
  }) {
    Text("Scan QR code")
  }
  Br()
  if (showFundScanner) {
    Video({
      id("fundChannelVideo")
      style {
        width(500.px)
        height(500.px)
        property("pointer-events", "none")
      }
    })
    DisposableEffect("fundChannelVideo") {
      val video = document.getElementById("fundChannelVideo").also { console.log("video", it) } as HTMLVideoElement
      qrScanner = QrScanner(video) { result ->
        console.log("decoded qr code: $result")
        result.split(';').apply {
          theirKeys = Channel.Keys(this[0], this[1], this[2])
          tokenId = this[3]
          theirAmount = this[4].toBigDecimal()
          theirAddress = this[5]
        }
        qrScanner!!.stop()
        showFundScanner = false
      }.also { it.start() }
      onDispose {
        qrScanner?.stop()
      }
    }
  }
}