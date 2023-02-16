package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.browser.window
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.Prefs
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.processRequest
import ltd.mbor.minipay.common.storage.createDB
import ltd.mbor.minipay.common.storage.getChannels
import ltd.mbor.minipay.common.storage.setChannelOpen
import ltd.mbor.minipay.common.subscribe
import scope
import view

val balances = mutableStateMapOf<String, Balance>()
val tokens = mutableStateMapOf<String, Token>()
var blockNumber by mutableStateOf(0)

external fun decodeURIComponent(encodedURI: String): String

fun getParams(parameterName: String): String? {
  val items = window.location.search.takeIf { it.length > 1 }?.substring(1)?.split("&") ?: emptyList()
  return items.asSequence().mapNotNull {
    val (name, value) = it.split("=");
    if (name == parameterName) decodeURIComponent(value) else null
  }.firstOrNull()
}

fun initMDS(prefs: Prefs) {
  scope.launch {
    MDS.init(prefs.uid, prefs.host, prefs.port) { msg: JsonElement ->
      when (msg.jsonString("event")) {
        "inited" -> {
          if (MDS.logging) console.log("Connected to Minima.")
          try {
            blockNumber = MDS.getBlockNumber()
            if (blockNumber <= 0) {
              window.alert("No blockes yet?")
              return@init
            }
          } catch (e: NullPointerException) {
            window.alert("Error getting status. Wrong UID?")
            return@init
          }
          balances.putAll(MDS.getBalances(confirmations = 0).associateBy { it.tokenId })
          tokens.putAll(MDS.getTokens().associateBy { it.tokenId })
          createDB()
          channels.addAll(getChannels(status = "OPEN"))
          channels.forEach { channel ->
            subscribe(channelKey(channel.my.keys, channel.tokenId), from = channel.updatedAt).onEach { msg ->
              log("tx msg: $msg")
              val splits = msg.split(";")
              if (splits[0].startsWith("TXN_UPDATE")) {
                channels.first { it.id == channel.id }.processUpdate(splits[0].endsWith("_ACK"), updateTxText = splits[1], settleTxText = splits[2])
              } else if (splits[0] == "TXN_REQUEST") {
                val (_, updateTxText, settleTxText) = splits
                channels.first { it.id == channel.id }.processRequest(updateTxText, settleTxText) {
                  events += it
                  view = "Channel events"
                }
              }
            }.onCompletion {
              log("completed")
            }.launchIn(scope)
          }
        }
      
        "NEWBALANCE" -> {
          val newBalances = MDS.getBalances(confirmations = 0).associateBy { it.tokenId }
          balances.clear()
          balances.putAll(newBalances)
          val newTokens = MDS.getTokens().associateBy { it.tokenId }
          tokens.clear()
          tokens.putAll(newTokens)
        }
      
        "NEWBLOCK" -> {
          blockNumber = msg.jsonObject["data"]!!.jsonObject["txpow"]!!.jsonObject["header"]!!.jsonString("block").toInt()
          if (multisigScriptAddress.isNotEmpty()) {
            scope.launch {
              val newBalances = MDS.getBalances(multisigScriptAddress, confirmations = 0)
              if (newBalances.any { it.confirmed > ZERO } && multisigScriptBalances.none { it.confirmed > ZERO }) {
                setChannelOpen(multisigScriptAddress)
              }
              multisigScriptBalances.clear()
              multisigScriptBalances.addAll(newBalances)
            }
          }
          if (eltooScriptAddress.isNotEmpty()) {
            scope.launch {
              eltooScriptCoins.put(eltooScriptAddress, MDS.getCoins(address = eltooScriptAddress))
            }
          }
        }
      }
    }
  }
}
