package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.PaymentRequestReceived
import ltd.mbor.minipay.common.model.Transport.FIREBASE
import ltd.mbor.minipay.common.storage.getChannels
import ltd.mbor.minipay.common.storage.updateChannel
import ltd.mbor.minipay.common.storage.updateChannelStatus

fun triggerScript(triggerSig1: String, triggerSig2: String) =
  "RETURN MULTISIG(2 $triggerSig1 $triggerSig2)"

fun eltooScript(blockDiff: Int = 256, updateSig1: String, updateSig2: String, settleSig1: String, settleSig2: String) = """
LET st=STATE(99)
LET ps=PREVSTATE(99)
IF st EQ ps AND @COINAGE GT $blockDiff AND MULTISIG(2 $settleSig1 $settleSig2) THEN
RETURN TRUE
ELSEIF st GT ps AND MULTISIG(2 $updateSig1 $updateSig2) THEN
RETURN TRUE
ENDIF
"""

fun channelKey(keys: Channel.Keys, tokenId: String) = listOf(keys.trigger, keys.update, keys.settle, tokenId).joinToString(";")

suspend fun MdsApi.newKeys(count: Int): List<String> {
  val command = List(count) { "keys action:new;" }.joinToString("\n")
  return cmd(command)!!.jsonArray.map { it.jsonObject["response"]!!.jsonString("publickey") }
}

suspend fun isPaymentChannelAvailable(toAddress: String, tokenId: String, amount: BigDecimal): Boolean {
  val matchingChannels = getChannels(status = "OPEN").filter { channel ->
    channel.their.address == toAddress && channel.tokenId == tokenId && channel.my.balance >= amount
  }
  return matchingChannels.isNotEmpty()
}

suspend fun signAndExportTx(id: Int, key: String): String {
  MDS.signTx(id, key)
  return MDS.exportTx(id)
}

suspend fun MdsApi.importAndPost(tx: String): JsonElement {
  val txId = newTxId()
  val txncreator = buildString{
    appendLine("txncreate id:$txId;")
    appendLine("txnimport id:$txId data:$tx;")
    append("txnpost id:$txId auto:true;")
  }
  val lastCmd = cmd(txncreator)!!.jsonArray.last()
  if (logging) log("importAndPost: ${lastCmd.jsonString("command")} ${lastCmd.jsonBoolean("status")}")
  return lastCmd.throwOnError()
}

fun JsonElement.throwOnError(): JsonElement {
  if (jsonBooleanOrNull("status") != true && !jsonStringOrNull("error").isNullOrBlank()) {
    log(jsonString("error"))
    throw MinimaException(jsonString("error"))
  }
  return this
}

private fun <T> Array<T>.sumOf(function: (T) -> BigDecimal) = fold(ZERO) { acc, it -> acc + function(it) }

suspend fun MdsApi.signFloatingTx(
  myKey: String,
  sourceScriptAddress: String,
  tokenId: String,
  states: Map<Int, String> = emptyMap(),
  vararg amountToAddress: Pair<BigDecimal, String>
): Int {
  
  val total = amountToAddress.sumOf { it.first }
  val txnId = newTxId()
  val txncreator = buildString {
    appendLine("txncreate id:$txnId;")
    appendLine("txninput id:$txnId address:${sourceScriptAddress} amount:${total.toPlainString()} tokenid:$tokenId floating:true;")
    states.mapNotNull { (index, value) -> value.takeUnless{ it.isEmpty() }?.let{ appendLine("txnstate id:$txnId port:$index value:$value;") } }
    amountToAddress.filter { it.first > ZERO }
      .forEach { (amount, address) -> appendLine("txnoutput id:$txnId amount:${amount.toPlainString()} tokenid:$tokenId address:$address;") }
    append("txnsign id:$txnId publickey:$myKey;")
  }
  
  cmd(txncreator)!!.jsonArray
  return txnId
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
    if(my.balance - amount > ZERO) appendLine("txnoutput id:$settleTxnId amount:${(my.balance - amount).toPlainString()} tokenid:${input.tokenId} address:${my.address};")
    if(their.balance + amount > ZERO) appendLine("txnoutput id:$settleTxnId amount:${(their.balance + amount).toPlainString()} tokenid:${input.tokenId} address:${their.address};")
    appendLine("txnsign id:$settleTxnId publickey:${my.keys.settle};")
    append("txnexport id:$settleTxnId;")
  }
  val settleTxn = MDS.cmd(settletxncreator)!!.jsonArray.last().jsonObject["response"]!!.jsonString("data")
  
  publish(
    channelKey(their.keys, tokenId),
    listOf(
      if(amount > ZERO) "TXN_UPDATE" else "TXN_REQUEST",
      updateTxn,
      settleTxn
    ).joinToString(";").also {
      log(it)
    }
  )
  
  return (updateTxn to updateTxnId) to (settleTxn to settleTxnId)
}

suspend fun Channel.request(amount: BigDecimal) = this.send(-amount)

suspend fun Channel.processRequest(updateTxText: String, settleTxText: String, onSuccess: (PaymentRequestReceived) -> Unit) {
  val updateTxId = newTxId()
  MDS.importTx(updateTxId, updateTxText)
  val settleTxId = newTxId()
  val settleTx = MDS.importTx(settleTxId, settleTxText)
  val channelBalance = (settleTx.outputs.firstOrNull{ it.address == my.address }?.tokenAmount ?: ZERO) to
    (settleTx.outputs.firstOrNull{ it.address == their.address }?.tokenAmount ?: ZERO)
  val newSequenceNumber = settleTx.state.first { it.port == 99 }.data.toInt()
  
  if (newSequenceNumber > sequenceNumber) onSuccess(
    PaymentRequestReceived(
      this,
      updateTxId,
      settleTxId,
      newSequenceNumber,
      channelBalance,
      FIREBASE
    )
  ) else log("Stale update $newSequenceNumber received for channel $id at $sequenceNumber")
}

suspend fun Channel.acceptRequestAndReply(updateTxId: Int, settleTxId: Int, sequenceNumber: Int, channelBalance: Pair<BigDecimal, BigDecimal>) {
  acceptRequest(updateTxId, settleTxId, sequenceNumber, channelBalance).let { (updateTx, settleTx) ->
    publish(channelKey(their.keys, tokenId), "TXN_UPDATE_ACK;$updateTx;$settleTx")
  }
}

suspend fun Channel.acceptRequest(updateTxId: Int, settleTxId: Int, sequenceNumber: Int, channelBalance: Pair<BigDecimal, BigDecimal>): Pair<String, String> {
  val signedUpdateTx = signAndExportTx(updateTxId, my.keys.update)
  val signedSettleTx = signAndExportTx(settleTxId, my.keys.settle)

  updateChannel(this, channelBalance, sequenceNumber, signedUpdateTx, signedSettleTx)

  return signedUpdateTx to signedSettleTx
}

suspend fun Channel.processUpdate(isAck: Boolean, updateTxText: String, settleTxText: String, onSuccess: (Channel) -> Unit): Channel {
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
  return update(signedUpdateTx, signedSettleTx, settleTx, onSuccess)
}

suspend fun Channel.update(updateTxText: String, settleTxText: String, settleTx: Transaction, onSuccess: (Channel) -> Unit): Channel {
  val sequenceNumber = settleTx.state.find { it.port == 99 }?.data?.toInt()!!
  val outputs = settleTx.outputs
  val myBalance = outputs.find { it.address == my.address }?.tokenAmount ?: ZERO
  val theirBalance = outputs.find { it.address == their.address }?.tokenAmount ?: ZERO

  return updateChannel(this, myBalance to theirBalance, sequenceNumber, updateTxText, settleTxText).also{
    onSuccess(it)
  }
}

suspend fun Channel.postUpdate(): Channel {
  MDS.importAndPost(updateTx)
  return updateChannelStatus(this, "UPDATED")
}

suspend fun Channel.triggerSettlement(): Channel {
  MDS.importAndPost(triggerTx)
  return updateChannelStatus(this, "TRIGGERED")
}

suspend fun Channel.completeSettlement(): Channel {
  MDS.importAndPost(settlementTx)
  return updateChannelStatus(this, "SETTLED")
}

suspend fun Channel.delete(): Channel {
  return updateChannelStatus(this, "DELETED")
}

