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
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.logic.channels
import ltd.mbor.minipay.logic.eltooScriptCoins
import ltd.mbor.minipay.logic.events
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewPrefs
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun MainView(
  inited: Boolean,
  prefs: Prefs,
  setPrefs: (Prefs) -> Unit,
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
  activity: MainActivity?,
  view: String,
  setView: (String) -> Unit
) {
  var showNavMenu by remember{ mutableStateOf(false) }

  fun toggleNavMenu() {
    showNavMenu = !showNavMenu
  }

  var channel by remember { mutableStateOf<Channel?>(null) }
  fun setChannel(newChannel: Channel?) {
    channel = newChannel
    setView(if (newChannel != null) "Channel details" else "Channels")
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(view) },
        navigationIcon = {
          IconButton(onClick = { toggleNavMenu() }) {
            Icon(Icons.Filled.Menu, contentDescription = null)
          }
        }
      )
    }
  ) {
    Menu(inited, showNavMenu, setView, startEmitting, stopEmitting) { showNavMenu = it }
    Column(Modifier.padding(it)) {
      when (view) {
        "MiniPay" -> Welcome(inited, setView)
        "Settings" -> Settings(prefs, setPrefs)
        "Receive" -> Receive(balances, tokens, address, setAddress, tokenId, setTokenId, amount, setAmount)
        "Send" -> Send(balances, address, setAddress, tokenId, setTokenId, amount, setAmount)
        "Channels" -> ChannelListing(channels, balances, eltooScriptCoins, activity, ::setChannel)
        "Create Channel" -> CreateChannel(balances, tokens, activity)
        "Channel Events" -> ChannelEvents(events, tokens, activity)
        "Channel Details" -> channel?.let{
          ChannelDetails(it, balances, activity, ::setChannel)
        }
      }
    }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewSend() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "", {}, ZERO, {}, "0x00", {}, {}, {}, null, "Send", {})
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewReceive() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "Receive", {})
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewChannels() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "Channels", {})
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewWelcome() {
  MiniPayTheme {
    MainView(false, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "MiniPay", {})
  }
}
