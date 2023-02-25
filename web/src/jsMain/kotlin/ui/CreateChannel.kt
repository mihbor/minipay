package ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getAddress
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.newKeys
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Text

@Composable
fun CreateChannel(
  isFunding: Boolean,
  balances: SnapshotStateMap<String, Balance>,
  tokens: SnapshotStateMap<String, Token>
) {

  var myKeys by remember { mutableStateOf(Channel.Keys("", "", "")) }
  var myAddress by remember { mutableStateOf("") }

  LaunchedEffect("createChannel") {
    MDS.newKeys(3).apply {
      myKeys = Channel.Keys(this[0], this[1], this[2])
    }
    myAddress = MDS.getAddress().address
  }

  Text("Trigger key: ${myKeys.trigger}")
  CopyToClipboard(myKeys.trigger)
  Br()
  Text("Update key: ${myKeys.update}")
  CopyToClipboard(myKeys.update)
  Br()
  Text("Settlement key: ${myKeys.settle}")
  CopyToClipboard(myKeys.settle)
  Br()
  Text("Address: $myAddress")
  CopyToClipboard(myAddress)
  Br()
  if (isFunding) FundChannel(myKeys, myAddress, balances, tokens)
  else RequestChannel(myKeys, myAddress, balances, tokens)
}