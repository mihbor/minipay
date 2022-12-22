package com.example.testapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testapp.Channel
import com.example.testapp.MainActivity
import com.example.testapp.ui.theme.TestAppTheme
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token

@Composable
fun MainView(
  inited: Boolean,
  uid: String,
  setUid: (String) -> Unit,
  balances: Map<String, Balance>,
  tokens: Map<String, Token>,
  address: String,
  setAddress: (String) -> Unit,
  amount: BigDecimal,
  setAmount: (BigDecimal?) -> Unit,
  tokenId: String,
  setTokenId: (String) -> Unit,
  startEmitting: () -> Unit,
  stopEmitting: () -> Unit,
  setRequestSentOnChannel: (Channel) -> Unit,
  activity: MainActivity?,
  view: String,
  setView: (String) -> Unit
) {

  var showNavMenu by remember{ mutableStateOf(false) }

  fun toggleNavMenu() {
    showNavMenu = !showNavMenu
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("MiniPay") },
        navigationIcon = {
          IconButton(onClick = { toggleNavMenu() }) {
            Icon(Icons.Filled.Menu, contentDescription = null)
          }
        }
      )
    }
  ) {
    Menu(inited, showNavMenu, setView, startEmitting, stopEmitting) { showNavMenu = it }
    when (view) {
      "settings" -> Column(modifier = Modifier.padding(it)) {
        Settings(uid, setUid)
      }
      "receive" -> Column(modifier = Modifier.padding(it)) {
        Receive(inited, balances, tokens, address, setAddress, tokenId, setTokenId, amount, setAmount)
      }
      "send" -> Column(modifier = Modifier.padding(it)) {
        Send(inited, balances, address, setAddress, tokenId, setTokenId, amount, setAmount)
      }
      "channels" -> Column(modifier = Modifier.padding(it)) {
        ChannelListing(activity, setRequestSentOnChannel)
      }
      "request-channel" -> Column(modifier = Modifier.padding(it)) {
        RequestChannel(balances, tokens, activity, setRequestSentOnChannel)
      }
      "fund-channel" -> Column(modifier = Modifier.padding(it)) {
        FundChannel(balances, tokens, activity, setRequestSentOnChannel)
      }
    }
  }
}

private val previewBalances = listOf(
  Balance("0x00", JsonNull, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "1"),
  Balance("0x01234567890", JsonPrimitive("test"), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "1"),
).associateBy { it.tokenId }

private val previewTokens = listOf(
  Token("0x00", JsonNull, BigDecimal.ONE, 1, null, null, null, JsonNull),
  Token("0x01234567890", JsonPrimitive("test"), BigDecimal.ONE, 1, null, null, null, JsonNull),
  Token("0x0999", JsonPrimitive("test2"), BigDecimal.ONE, 1, null, null, null, JsonNull),
).associateBy { it.tokenId }

@Preview(showBackground = true)
@Composable
fun ViewSend() {
  TestAppTheme {
    MainView(true, "uid123", {}, previewBalances, previewTokens, "", {}, ZERO, {}, "0x00", {}, {}, {}, {}, null, "send", {})
  }
}

@Preview(showBackground = true)
@Composable
fun ViewRecieve() {
  TestAppTheme {
    MainView(true, "uid456", {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, {}, null, "receive", {})
  }
}