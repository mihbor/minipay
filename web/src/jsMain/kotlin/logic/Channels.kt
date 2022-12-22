package logic

import Channel
import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

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

suspend fun newKeys(count: Int): List<String> {
  val command = List(count) { "keys action:new;" }.joinToString("\n")
  return MDS.cmd(command)!!.jsonArray.map { it.jsonObject["response"]!!.jsonString("publickey")!! }
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

suspend fun importAndPost(tx: String): JsonElement? {
  val txId = newTxId()
  MDS.importTx(txId, tx)
  return MDS.post(txId)
}

suspend fun signFloatingTx(
  myKey: String,
  sourceScriptAddress: String,
  states: Map<Int, String> = emptyMap(),
  tokenId: String,
  vararg amountToAddress: Pair<BigDecimal, String>
): Int {
  
  val total = amountToAddress.sumOf { it.first }
  val txnId = newTxId()
  val txncreator = buildString {
    appendLine("txncreate id:$txnId;")
    appendLine("txninput id:$txnId address:${sourceScriptAddress} amount:${total.toPlainString()} tokenid:$tokenId floating:true;")
    states.mapNotNull { (index, value) -> value.takeUnless{ it.isEmpty() }?.let{ appendLine("txnstate id:$txnId port:$index value:$value;") } }
    amountToAddress.forEach { (amount, address) -> appendLine("txnoutput id:$txnId amount:${amount.toPlainString()} tokenid:$tokenId address:$address;") }
    append("txnsign id:$txnId publickey:$myKey;")
  }
  
  MDS.cmd(txncreator)!!.jsonArray
  return txnId
}

private fun <T> Array<T>.sumOf(function: (T) -> BigDecimal) = fold(ZERO) { acc, it -> acc + function(it) }

suspend fun Channel.update(isAck: Boolean, updateTx: String, settleTx: String): Channel {
  log("Updating channel isAck:$isAck")
  val updateTxnId = newTxId()
  MDS.importTx(updateTxnId, updateTx)
  val settleTxnId = newTxId()
  val importedSettleTx = MDS.importTx(settleTxnId, settleTx)
  
  if (!isAck) {
    val signedUpdateTx = signAndExportTx(updateTxnId, my.keys.update)
    val signedSettleTx = signAndExportTx(settleTxnId, my.keys.settle)
    publish(channelKey(their.keys, tokenId), listOf("TXN_UPDATE_ACK", signedUpdateTx, signedSettleTx).joinToString(";"))
  }
  val outputs = importedSettleTx.outputs
  val myBalance = outputs.find { it.miniAddress == my.address }?.tokenAmount
  val theirBalance = outputs.find { it.miniAddress == their.address }?.tokenAmount
  val sequenceNumber = importedSettleTx.state.find { it.port == 99 }?.data?.toInt()
  
  return if (myBalance == null || theirBalance == null) this.also {
    log("balance for my address ${my.address}: $myBalance, balance for their address ${their.address}: $theirBalance")
  } else updateChannel(this, myBalance to theirBalance, sequenceNumber!!, updateTx, settleTx).also {
    channels[channels.indexOf(this)] = it
  }
}

suspend fun Channel.request(amount: BigDecimal) = this.send(-amount)

suspend fun Channel.send(amount: BigDecimal) {
  val currentSettlementTx = MDS.importTx(newTxId(), settlementTx)
  val input = currentSettlementTx.inputs.first()
  val state = currentSettlementTx.state.find{ it.port == 99 }!!.data
  val updateTxnId = newTxId()
  val updatetxncreator = buildString {
    appendLine("txncreate id:$updateTxnId;")
    appendLine("txninput id:$updateTxnId address:${input.address} amount:${input.amount} tokenid:${input.tokenId} floating:true;")
    appendLine("txnstate id:$updateTxnId port:99 value:${state.toInt() + 1};")
    appendLine("txnoutput id:$updateTxnId amount:${input.amount} tokenid:${input.tokenId} address:${input.address};")
    appendLine("txnsign id:$updateTxnId publickey:${my.keys.update};")
    append("txnexport id:$updateTxnId;")
  }
  val updateTxn = MDS.cmd(updatetxncreator)!!.jsonArray.last()
  val settleTxnId = newTxId()
  val settletxncreator = buildString {
    appendLine("txncreate id:$settleTxnId;")
    appendLine("txninput id:$settleTxnId address:${input.address} amount:${input.amount} tokenid:${input.tokenId} floating:true;")
    appendLine("txnstate id:$settleTxnId port:99 value:${state.toInt() + 1};")
    if(my.balance - amount > ZERO) appendLine("txnoutput id:$settleTxnId amount:${(my.balance - amount).toPlainString()} tokenid:${input.tokenId} address:${my.address};")
    if(their.balance + amount > ZERO) appendLine("txnoutput id:$settleTxnId amount:${(their.balance + amount).toPlainString()} tokenid:${input.tokenId} address:${their.address};")
    appendLine("txnsign id:$settleTxnId publickey:${my.keys.settle};")
    append("txnexport id:$settleTxnId;")
  }
  val settleTxn = MDS.cmd(settletxncreator)!!.jsonArray.last()
  
  publish(
    channelKey(their.keys, tokenId),
    listOf(
      if(amount > ZERO) "TXN_UPDATE" else "TXN_REQUEST",
      updateTxn.jsonObject["response"]!!.jsonString("data"),
      settleTxn.jsonObject["response"]!!.jsonString("data")
    ).joinToString(";")
  )
}

suspend fun Channel.postUpdate(): Channel {
  val response = importAndPost(updateTx)
  return if (response == null) this
  else updateChannelStatus(this, "UPDATED")
}

suspend fun Channel.triggerSettlement(): Channel {
  val response = importAndPost(triggerTx)
  return if (response == null) this
  else updateChannelStatus(this, "TRIGGERED")
}

suspend fun Channel.completeSettlement(): Channel {
  val response = importAndPost(settlementTx)
  return if (response == null) this
  else updateChannelStatus(this, "SETTLED")
}
