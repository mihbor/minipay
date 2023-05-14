package ltd.mbor.minipay.common

import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelEvent
import ltd.mbor.minipay.common.model.PaymentRequestSent
import ltd.mbor.minipay.common.transport.Transport

class ChannelService(
  val mds: MdsApi,
  val storage: ChannelStorage,
  val transport: Transport,
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
    
    val signedUpdateTx = if (isAck) updateTxText else mds.signAndExportTx(updateTxnId, my.keys.update)
    val signedSettleTx = if (isAck) settleTxText else mds.signAndExportTx(settleTxnId, my.keys.settle)
    if (!isAck) {
      transport.publish(channelKey(their.keys, tokenId), listOf("TXN_UPDATE_ACK", signedUpdateTx, signedSettleTx).joinToString(";"))
    }
    return update(signedUpdateTx, signedSettleTx, settleTx, onSuccess)
  }

  suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String): Channel {
    return processUpdate(isAck, updateTxText, settleTxText)  {
      channels.put(it)
      if (isAck) events.removeIf { it is PaymentRequestSent && it.channel.id == id }
    }
  }

  suspend fun processMessage(
    msg: String,
    onUpdate: (Channel, Boolean) -> Unit,
    onUnhandled: suspend (List<String>) -> Unit,
    getChannelId: () -> Uuid,
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
    getChannelId: (() -> Uuid),
    onUpdate: (Channel, Boolean) -> Unit = { _, _ -> },
    onUnhandled: suspend (List<String>) -> Unit,
    onEvent: (ChannelEvent) -> Unit
  ) {
    transport.subscribe(this).onEach { msg ->
      processMessage(msg, onUpdate, onUnhandled, getChannelId, onEvent)
    }.onCompletion {
      log("completed")
    }.launchIn(scope)
  }

  suspend fun Channel.send(amount: BigDecimal): Pair<Pair<String, Int>, Pair<String, Int>> {
    val currentSettlementTx = MDS.importTx(newTxId(), settlementTx)
    val input = currentSettlementTx.inputs.first()
    val updateTxnId = newTxId()
    val updatetxncreator = buildString {
      appendLine("txncreate id:$updateTxnId;")
      appendLine("txninput id:$updateTxnId address:${input.address} amount:${input.amount} tokenid:${input.tokenId} floating:true;")
      appendLine("txnstate id:$updateTxnId port:99 value:${sequenceNumber + 1};")
      appendLine("txnoutput id:$updateTxnId amount:${input.amount} tokenid:${input.tokenId} address:${input.address};")
      appendLine("txnsign id:$updateTxnId publickey:${my.keys.update};")
      append("txnexport id:$updateTxnId;")
    }
    val updateTxn = MDS.cmd(updatetxncreator)!!.jsonArray.last().jsonObject["response"]!!.jsonString("data")
    val settleTxnId = newTxId()
    val settletxncreator = buildString {
      appendLine("txncreate id:$settleTxnId;")
      appendLine("txninput id:$settleTxnId address:${input.address} amount:${input.amount} tokenid:${input.tokenId} floating:true;")
      appendLine("txnstate id:$settleTxnId port:99 value:${sequenceNumber + 1};")
      if(my.balance - amount > BigDecimal.ZERO) appendLine("txnoutput id:$settleTxnId amount:${(my.balance - amount).toPlainString()} tokenid:${input.tokenId} address:${my.address};")
      if(their.balance + amount > BigDecimal.ZERO) appendLine("txnoutput id:$settleTxnId amount:${(their.balance + amount).toPlainString()} tokenid:${input.tokenId} address:${their.address};")
      appendLine("txnsign id:$settleTxnId publickey:${my.keys.settle};")
      append("txnexport id:$settleTxnId;")
    }
    val settleTxn = MDS.cmd(settletxncreator)!!.jsonArray.last().jsonObject["response"]!!.jsonString("data")

    transport.publish(
      channelKey(their.keys, tokenId),
      listOf(
        if(amount > BigDecimal.ZERO) "TXN_UPDATE" else "TXN_REQUEST",
        updateTxn,
        settleTxn
      ).joinToString(";").also {
        log(it)
      }
    )

    return (updateTxn to updateTxnId) to (settleTxn to settleTxnId)
  }

  suspend fun Channel.request(amount: BigDecimal) = this.send(-amount)

  suspend fun Channel.acceptRequestAndReply(updateTxId: Int, settleTxId: Int, sequenceNumber: Int, channelBalance: Pair<BigDecimal, BigDecimal>) {
    acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance).let { (updateTx, settleTx) ->
      transport.publish(channelKey(their.keys, tokenId), "TXN_UPDATE_ACK;$updateTx;$settleTx")
    }
  }

  suspend fun Channel.commitFund(
    triggerTx: String,
    settlementTx: String,
    fundingTx: String,
    theirInputCoins: List<String>,
    theirInputScripts: List<String>,
    key: String = "auto"
  ): Channel {
    mds.importTx(newTxId(), triggerTx)
    mds.importTx(newTxId(), settlementTx)
    val fundingTxId = newTxId()
    mds.importTx(fundingTxId, fundingTx).outputs
    theirInputCoins.forEach { mds.importCoin(it) }
    theirInputScripts.forEach { mds.newScript(it) }

    val txncreator = buildString {
      appendLine("txnsign id :$fundingTxId publickey:$key;")
      appendLine("txnpost id :$fundingTxId auto:true;")
      append("txndelete id :$fundingTxId;")
    }
    val result = mds.cmd(txncreator)!!.jsonArray
    val status = result.first{ it.jsonString("command") == "txnpost" }.jsonString("status")
    log("txnpost status: $status")

    return if (status.toBoolean()) {
      storage.updateChannel(this, triggerTx, settlementTx)
    } else this
  }
}

fun List<Channel>.forId(id: Uuid) = first { it.id == id }

fun MutableList<Channel>.put(channel: Channel) {
  val current = firstOrNull{ it.id == channel.id }
  if (current != null) set(indexOf(current), channel)
  else add(channel)
}

fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
  removeAll(filter(predicate))
}
