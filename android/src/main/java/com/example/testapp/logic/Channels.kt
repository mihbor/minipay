package com.example.testapp.logic

import androidx.compose.runtime.*
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.*

val channels = mutableStateListOf<Channel>()
var multisigScriptAddress by mutableStateOf("")
var eltooScriptAddress by mutableStateOf("")
val multisigScriptBalances = mutableStateListOf<Balance>()
val eltooScriptCoins = mutableStateMapOf<String, List<Coin>>()

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

  return if (myBalance == null || theirBalance == null) this.also{
    log("balance for my address ${my.address}: $myBalance, balance for their address ${their.address}: $theirBalance")
  } else updateChannel(this, myBalance to theirBalance, sequenceNumber!!, updateTx, settleTx).also{
    channels[channels.indexOf(this)] = it
  }
}

suspend fun channelUpdateAck(updateTxText: String, settleTxText: String) {

  MDS.importTx(newTxId(), updateTxText).also { updateTx ->
    val settleTx = MDS.importTx(newTxId(), settleTxText)
    val channel = getChannel(updateTx.outputs.first().address)!!
    val sequenceNumber = settleTx.state.find { it.port == 99 }?.data?.toInt()!!

    val outputs = settleTx.outputs
    val channelBalance = outputs.find { it.miniAddress == channel.my.address }!!.amount to outputs.find { it.miniAddress == channel.their.address }!!.amount
    updateChannel(channel, channelBalance, sequenceNumber, updateTxText, settleTxText)
  }
}

suspend fun Channel.acceptRequest(updateTx: Pair<Int, Transaction>, settleTx: Pair<Int, Transaction>): Pair<String, String> {
  val sequenceNumber = settleTx.second.state.find { it.port == 99 }?.data?.toInt()

  val outputs = settleTx.second.outputs
  val channelBalance = outputs.find { it.miniAddress == my.address }!!.amount to outputs.find { it.miniAddress == their.address }!!.amount

  val signedUpdateTx = signAndExportTx(updateTx.first, my.keys.update)
  val signedSettleTx = signAndExportTx(settleTx.first, my.keys.settle)

  updateChannel(this, channelBalance, sequenceNumber!!, signedUpdateTx, signedSettleTx)

  return signedUpdateTx to signedSettleTx
}
