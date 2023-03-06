package ltd.mbor.minipay.logic

import androidx.compose.runtime.*
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.importTx
import ltd.mbor.minipay.MainActivity
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.storage.getChannel
import ltd.mbor.minipay.sendDataToService
import ltd.mbor.minipay.view

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

val events = mutableStateListOf<ChannelEvent>()

lateinit var channelService: ChannelService

suspend fun Channel.acceptRequestAndEmitResponse(updateTxId: Int, settleTxId: Int, sequenceNumber: Int, channelBalance: Pair<BigDecimal, BigDecimal>, activity: MainActivity) {
  val (updateTx, settleTx) = acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance)
  activity.disableReaderMode()
  activity.sendDataToService("TXN_UPDATE_ACK;$updateTx;$settleTx")
}

suspend fun channelUpdateAck(updateTxText: String, settleTxText: String) {
  val updateTx = MDS.importTx(newTxId(), updateTxText)
  val settleTx = MDS.importTx(newTxId(), settleTxText)
  val channel = getChannel(updateTx.outputs.first().address)!!
  with(channelService) {
    channel.update(updateTxText, settleTxText, settleTx) {
      channels.put(it)
      events.removeIf { it.channel.id == channel.id && it is PaymentRequestSent }
    }
  }
}

fun Channel.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Unit = { }
) {
  with(channelService) {
    channelKey(my.keys, tokenId).subscribe({ id }, onUpdate, onUnhandled) {
      view = "Channel events"
    }
  }
}

fun String.subscribe(
  onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
  onUnhandled: suspend (List<String>) -> Uuid
) {
  var channelId: Uuid? = null
  with(channelService) {
    subscribe({ checkNotNull(channelId) }, onUpdate, {
      channelId = onUnhandled(it)
    }, {
      view = "Channel events"
    })
  }
}
