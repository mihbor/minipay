package ltd.mbor.minipay.logic

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import ltd.mbor.minimak.*
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.storage.getChannel
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.sendDataToService
import ltd.mbor.minipay.view

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
  return processUpdate(isAck, updateTxText, settleTxText)  {
    channels.put(it)
    if (isAck) events.removeIf { it.channel.id == id && it is PaymentRequestSent }
  }
}

suspend fun Channel.acceptRequestAndEmitResponse(updateTxId: Int, settleTxId: Int, sequenceNumber: Int, channelBalance: Pair<BigDecimal, BigDecimal>, activity: MainActivity) {
  val (updateTx, settleTx) = acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance)
  activity.disableReaderMode()
  activity.sendDataToService("TXN_UPDATE_ACK;$updateTx;$settleTx")
}

suspend fun channelUpdateAck(updateTxText: String, settleTxText: String) {
  val updateTx = MDS.importTx(newTxId(), updateTxText)
  val settleTx = MDS.importTx(newTxId(), settleTxText)
  val channel = getChannel(updateTx.outputs.first().address)!!
  channel.update(updateTxText, settleTxText, settleTx) {
    channels.put(it)
    events.removeIf { it.channel.id == channel.id && it is PaymentRequestSent }
  }
}

fun Channel.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Unit = { }
) {
  channelKey(my.keys, tokenId).subscribe({ id }, onUpdate, onUnhandled)
}

fun String.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Int
) {
  var channelId: Int? = null
  subscribe({ checkNotNull(channelId) }, onUpdate) {
    channelId = onUnhandled(it)
  }
}

fun String.subscribe(
  getChannelId: (() -> Int),
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Unit
) {
  subscribe(this).onEach { msg ->
    log("tx msg: $msg")
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channels.forId(getChannelId()).processUpdate(isAck, updateTxText = splits[1], settleTxText = splits[2]).also {
        onUpdate(it, isAck)
      }
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      channels.forId(getChannelId()).processRequest(updateTxText, settleTxText) {
        events += it
        view = "Channel events"
      }
    } else {
      onUnhandled(splits)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}
