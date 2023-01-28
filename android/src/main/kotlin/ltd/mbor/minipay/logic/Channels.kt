package ltd.mbor.minipay.logic

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

suspend fun channelUpdateAck(updateTxText: String, settleTxText: String) {
  val updateTx = MDS.importTx(newTxId(), updateTxText)
  val settleTx = MDS.importTx(newTxId(), settleTxText)
  val channel = getChannel(updateTx.outputs.first().address)!!
  channel.update(updateTxText, settleTxText, settleTx) {
    channels[channels.indexOf(channels.first{ it.id == channel.id })] = it
    events.removeIf { it.channel.id == channel.id && it is PaymentRequestSent }
  }
}

suspend fun MutableList<Channel>.reload() {
  val newChannels = getChannels().map { channel ->
    val eltooCoins = MDS.getCoins(address = channel.eltooAddress)
    eltooScriptCoins[channel.eltooAddress] = eltooCoins
    if (channel.status == "OPEN" && eltooCoins.isNotEmpty()) updateChannelStatus(channel, "TRIGGERED")
    else if (channel.status in listOf("TRIGGERED", "UPDATED") && eltooCoins.isEmpty()) {
      val anyTransactionsFromEltoo = MDS.getTransactions(channel.eltooAddress)
        ?.any{ it.inputs.any { it.address == channel.eltooAddress } } ?: false
      if (anyTransactionsFromEltoo) updateChannelStatus(channel, "SETTLED")
      else channel
    }
    else channel
  }
  clear()
  addAll(newChannels)
}