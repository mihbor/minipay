package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.log
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.JoinChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import scope
import view

fun joinChannel(
  myAddress: String,
  myKeys: Channel.Keys,
  tokenId: String,
  amount: BigDecimal,
  onEvent: (JoinChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  var channel: Channel? = null
  subscribe(channelKey(myKeys, tokenId)).onEach { msg ->
    log("tx msg: $msg")
    
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channel = channel!!.processUpdate(isAck, updateTxText = splits[1], settleTxText = splits[2])
      onEvent(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      channel!!.processRequest(updateTxText, settleTxText) {
        events += it
        view = "Channel events"
      }
    } else {
      val timeLock = splits[0].toInt()
      val theirKeys = Channel.Keys(splits[1], splits[2], splits[3])
      val triggerTx = splits[4]
      val settlementTx = splits[5]
      val fundingTx = splits[6]
      onEvent(SIGS_RECEIVED, null)
      multisigScriptAddress = MDS.newScript(triggerScript(theirKeys.trigger, myKeys.trigger)).address
      eltooScriptAddress = MDS.newScript(eltooScript(timeLock, theirKeys.update, myKeys.update, theirKeys.settle, myKeys.settle)).address
      onEvent(SCRIPTS_DEPLOYED, null)
      joinChannel(myAddress, myKeys, theirKeys, tokenId, amount, triggerTx, settlementTx, fundingTx, timeLock, multisigScriptAddress, eltooScriptAddress, onEvent)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}
