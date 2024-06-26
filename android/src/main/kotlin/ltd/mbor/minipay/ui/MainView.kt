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
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.channelInvite
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.logic.channels
import ltd.mbor.minipay.logic.eltooScriptCoins
import ltd.mbor.minipay.logic.events
import ltd.mbor.minipay.ui.channels.ChannelDetails
import ltd.mbor.minipay.ui.channels.ChannelEvents
import ltd.mbor.minipay.ui.channels.ChannelListing
import ltd.mbor.minipay.ui.channels.CreateChannel
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
  setView: (String) -> Unit,
  channelInvite: ChannelInvite,
  setChannelInvite: (ChannelInvite) -> Unit
) {
  var showNavMenu by remember{ mutableStateOf(false) }
  var maximaContact by remember { mutableStateOf<Contact?>(null) }

  fun toggleNavMenu() {
    showNavMenu = !showNavMenu
  }

  var channel by remember { mutableStateOf<Channel?>(null) }
  fun setChannel(newChannel: Channel?) {
    channel = newChannel
    setView(if (newChannel != null) "Channel Details" else "Channels")
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
        "Receive" -> Receive(balances, tokens, address, setAddress, tokenId, setTokenId, amount, setAmount)
        "Send" -> Send(balances, address, setAddress, tokenId, setTokenId, amount, setAmount)
        "Create Channel" -> CreateChannel(channels, balances, tokens, activity, channelInvite, setChannelInvite, maximaContact) { maximaContact = it }
        "Channels" -> ChannelListing(channels, balances, eltooScriptCoins, activity, ::setChannel)
        "Channel Events" -> ChannelEvents(events, tokens, activity)
        "Channel Details" -> channel?.let{
          ChannelDetails(it, balances, activity, ::setChannel)
        }
        "Contacts" -> Contacts {
          maximaContact = it
          setView("Create Channel")
        }
        "Settings" -> Settings(prefs, setPrefs)
        "Help" -> Help()
      }
    }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewSend() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "", {}, ZERO, {}, "0x00", {}, {}, {}, null, "Send", {}, channelInvite) { channelInvite = it }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewReceive() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "Receive", {}, channelInvite) { channelInvite = it }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewChannels() {
  MiniPayTheme {
    MainView(true, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "Channels", {}, channelInvite) { channelInvite = it }
  }
}

@Composable @Preview(showBackground = true)
fun PreviewMainViewWelcome() {
  MiniPayTheme {
    MainView(false, previewPrefs, {}, previewBalances, previewTokens, "address", {}, BigDecimal.ONE, {}, "0x01234567890", {}, {}, {}, null, "MiniPay", {}, channelInvite) { channelInvite = it }
  }
}
