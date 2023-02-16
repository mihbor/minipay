package ltd.mbor.minipay.logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.log
import ltd.mbor.minimak.newScript
import ltd.mbor.minipay.common.*
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.scope
import ltd.mbor.minipay.view

suspend fun fundChannel(
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  theirAddress: String,
  myAmount: BigDecimal,
  theirAmount: BigDecimal,
  tokenId: String,
  timeLock: Int,
  event: (FundChannelEvent, Channel?) -> Unit = { _, _ -> }
) {
  multisigScriptAddress = MDS.newScript(triggerScript(myKeys.trigger, theirKeys.trigger)).address
  eltooScriptAddress = MDS.newScript(eltooScript(timeLock, myKeys.update, theirKeys.update, myKeys.settle, theirKeys.settle)).address
  event(SCRIPTS_DEPLOYED, null)
  
  var channel = prepareFundChannel(myKeys, theirKeys, theirAddress, myAmount, theirAmount, tokenId, timeLock, multisigScriptAddress, eltooScriptAddress, event)
  
  subscribe(channelKey(myKeys, tokenId)).onEach { msg ->
    log("tx msg: $msg")
    val splits = msg.split(";")
    if (splits[0].startsWith("TXN_UPDATE")) {
      val isAck = splits[0].endsWith("_ACK")
      channel = channel.processUpdate(isAck, updateTxText = splits[1], settleTxText = splits[2])
      event(if (isAck) CHANNEL_UPDATED_ACKED else CHANNEL_UPDATED, channel)
    } else if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      channel.processRequest(updateTxText, settleTxText) {
        events += it
        view = "Channel events"
      }
    } else {
      val (triggerTx, settlementTx, fundingTx) = splits
      val (theirInputCoins, theirInputScripts) = splits.subList(3, splits.size)
        .let{ it.takeUnless { it.isEmpty() }?.chunked(it.size/2) ?: listOf(emptyList(), emptyList()) }
      event(SIGS_RECEIVED, channel)
      channel = channel.commitFund("auto", triggerTx, settlementTx, fundingTx, theirInputCoins, theirInputScripts)
      event(CHANNEL_FUNDED, channel)
    }
  }.onCompletion {
    log("completed")
  }.launchIn(scope)
}
