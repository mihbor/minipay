package logic

import androidx.compose.runtime.*
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.*

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

suspend fun Channel.update(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
  log("Updating channel isAck:$isAck")
  val updateTxnId = newTxId()
  MDS.importTx(updateTxnId, updateTxText)
  val settleTxnId = newTxId()
  val settleTx = MDS.importTx(settleTxnId, settleTxText)
  
  val signedUpdateTx = if (isAck) updateTxText else signAndExportTx(updateTxnId, my.keys.update)
  val signedSettleTx = if (isAck) settleTxText else signAndExportTx(settleTxnId, my.keys.settle)
  if (!isAck) {
    publish(channelKey(their.keys, tokenId), listOf("TXN_UPDATE_ACK", signedUpdateTx, signedSettleTx).joinToString(";"))
  }
  return update(signedUpdateTx, signedSettleTx, settleTx) {
    channels[channels.indexOf(channels.first{ it.id == id })] = it
    if (isAck) events.removeIf { it.channel.id == id && it is PaymentRequestSent }
  }
}

fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
  removeAll(filter(predicate))
}

suspend fun MutableList<Channel>.reload(eltooScriptCoins: MutableMap<String, List<Coin>>) {
  channelService.reloadChannels(this, eltooScriptCoins)
}