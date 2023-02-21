package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent

class ChannelService(
  val mds: MdsApi,
  val storage: ChannelStorage,
  val channels: MutableList<Channel>,
  val events: MutableList<ChannelEvent>
) {
  suspend fun reloadChannels(eltooScriptCoins: MutableMap<String, List<Coin>>) {
    val newChannels = storage.getChannels().map { channel ->
      when (channel.status) {
        "OFFERED" -> {
          val multiSigCoins = mds.getCoins(address = channel.multiSigAddress)
          if (multiSigCoins.isNotEmpty()) storage.updateChannelStatus(channel, "OPEN")
          else channel
        }
        in setOf("OPEN", "TRIGGERED", "UPDATED") -> {
          val eltooCoins = mds.getCoins(address = channel.eltooAddress)
          eltooScriptCoins[channel.eltooAddress] = eltooCoins
          if (channel.status == "OPEN" && eltooCoins.isNotEmpty()) storage.updateChannelStatus(channel, "TRIGGERED")
          else if (channel.status in listOf("TRIGGERED", "UPDATED") && eltooCoins.isEmpty()) {
            val anyTransactionsFromEltoo = mds.getTransactions(channel.eltooAddress)
              ?.any { it.inputs.any { it.address == channel.eltooAddress } } ?: false
            if (anyTransactionsFromEltoo) storage.updateChannelStatus(channel, "SETTLED")
            else channel
          } else channel
        }
        else -> channel
      }
    }
    channels.clear()
    channels.addAll(newChannels)
  }
  
  suspend fun Channel.update(updateTxText: String, settleTxText: String, settleTx: Transaction, onSuccess: (Channel) -> Unit): Channel {
    val sequenceNumber = settleTx.state.find { it.port == 99 }?.data?.toInt()!!
    val outputs = settleTx.outputs
    val myBalance = outputs.find { it.address == my.address }?.tokenAmount ?: BigDecimal.ZERO
    val theirBalance = outputs.find { it.address == their.address }?.tokenAmount ?: BigDecimal.ZERO
    
    return storage.updateChannel(this, myBalance to theirBalance, sequenceNumber, updateTxText, settleTxText).also{
      onSuccess(it)
    }
  }
  
  suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String, onSuccess: (Channel) -> Unit): Channel {
    log("Updating channel isAck:$isAck")
    val updateTxnId = newTxId()
    mds.importTx(updateTxnId, updateTxText)
    val settleTxnId = newTxId()
    val settleTx = mds.importTx(settleTxnId, settleTxText)
    
    val signedUpdateTx = if (isAck) updateTxText else signAndExportTx(updateTxnId, my.keys.update)
    val signedSettleTx = if (isAck) settleTxText else signAndExportTx(settleTxnId, my.keys.settle)
    if (!isAck) {
      publish(channelKey(their.keys, tokenId), listOf("TXN_UPDATE_ACK", signedUpdateTx, signedSettleTx).joinToString(";"))
    }
    return update(signedUpdateTx, signedSettleTx, settleTx, onSuccess)
  }

  suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
    return processUpdate(isAck, updateTxText, settleTxText)  {
      channels.put(it)
      if (isAck) events.removeIf { it.channel.id == id && it is PaymentRequestSent }
    }
  }

  suspend fun processMessage(
    msg: String,
    onUpdate: (Channel, Boolean) -> Unit,
    onUnhandled: suspend (List<String>) -> Unit,
    getChannelId: () -> Int,
    onEvent: (ChannelEvent) -> Unit
  ) {
    log("tx msg: $msg")
    fun getChannel() = channels.forId(getChannelId())
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      getChannel().processUpdate(isAck, updateTxText = splits[1], settleTxText = splits[2]).also {
        onUpdate(it, isAck)
      }
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      getChannel().processRequest(updateTxText, settleTxText) {
        events += it
        onEvent(it)
      }
    } else {
      onUnhandled(splits)
    }
  }
  
  fun String.subscribe(
    getChannelId: (() -> Int),
    onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
    onUnhandled: suspend (List<String>) -> Unit,
    onEvent: (ChannelEvent) -> Unit
  ) {
    subscribe(this).onEach { msg ->
      processMessage(msg, onUpdate, onUnhandled, getChannelId, onEvent)
    }.onCompletion {
      log("completed")
    }.launchIn(scope)
  }
}

fun List<Channel>.forId(id: Int) = first { it.id == id }

fun MutableList<Channel>.put(channel: Channel) {
  val current = firstOrNull{ it.id == channel.id }
  if (current != null) set(indexOf(current), channel)
  else add(channel)
}

fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
  removeAll(filter(predicate))
}
