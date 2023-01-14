package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.logic.channels
import ltd.mbor.minipay.logic.eltooScriptCoins
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

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
        Receive(balances, tokens, address, setAddress, tokenId, setTokenId, amount, setAmount)
      }
      "send" -> Column(modifier = Modifier.padding(it)) {
        Send(balances, address, setAddress, tokenId, setTokenId, amount, setAmount)
      }
      "channels" -> Column(modifier = Modifier.padding(it)) {
        ChannelListing(channels, balances, eltooScriptCoins, activity, setRequestSentOnChannel)
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

@Composable @Preview(showBackground = true)
fun PreviewMainViewSend() {
  MiniPayTheme {
    MainView(true, "uid123", {}, previewBalances, previewTokens, "", {}, ZERO, {}, "0x00", {}, {}, {}, {}, null, "send", {})
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewReceive() {
  MiniPayTheme {
    MainView(true, "uid456", {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, {}, null, "receive", {})
  }
}