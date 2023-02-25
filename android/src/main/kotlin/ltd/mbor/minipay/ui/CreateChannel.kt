package ltd.mbor.minipay.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getAddress
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.newKeys
import ltd.mbor.minipay.ui.preview.previewBalances
import ltd.mbor.minipay.ui.preview.previewTokens
import ltd.mbor.minipay.ui.theme.MiniPayTheme

@Composable
fun CreateChannel(isFunding: Boolean, balances: Map<String, Balance>, tokens: Map<String, Token>, activity: MainActivity?) {

  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var myAddress by remember { mutableStateOf("") }

  LaunchedEffect("createChannel") {
    MDS.newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    myAddress = MDS.getAddress().address
  }

  LazyColumn {
    item {
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
        if (isFunding) FundChannel(myKeys, myAddress, balances, tokens, activity)
        else RequestChannel(myKeys, myAddress, balances, tokens, activity)
      }
    }
  }
}

@Composable @Preview
fun PreviewCreateChannelFund() {
  MiniPayTheme {
    CreateChannel(true, previewBalances, previewTokens, null)
  }
}

@Composable @Preview
fun PreviewCreateChannelJoin() {
  MiniPayTheme {
    CreateChannel(false, previewBalances, previewTokens, null)
  }
}
