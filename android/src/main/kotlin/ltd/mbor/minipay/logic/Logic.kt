package ltd.mbor.minipay

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import io.ktor.utils.io.errors.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.channelKey
import ltd.mbor.minipay.common.storage.createDB
import ltd.mbor.minipay.common.storage.getChannels
import ltd.mbor.minipay.common.storage.setChannelOpen
import ltd.mbor.minipay.logic.*

var inited by mutableStateOf(false)
val balances = mutableStateMapOf<String, Balance>()
val tokens = mutableStateMapOf<String, Token>()
var blockNumber by mutableStateOf(0)

suspend fun initMDS(uid: String, host: String, port: Int, context: Context) {
  inited = false
  MDS.init(uid, host, port, logging = true) { msg ->
    when(msg.jsonObject["event"]?.jsonPrimitive?.content) {
      "inited" -> {
        if (MDS.logging) Log.i(TAG, "Connected to Minima.")
        try {
          blockNumber = MDS.getBlockNumber()
          if (blockNumber <= 0) {
            Toast.makeText(context, "No blocks yet? Minima disconnected?", Toast.LENGTH_LONG).show()
            return@init
          }
        } catch (e: NullPointerException) {
          Toast.makeText(context, "Error getting status. Wrong UID?", Toast.LENGTH_LONG).show()
          log(e.toString())
          return@init
        } catch (e: IOException) {
          Toast.makeText(context, "Error connecting. Wrong host or port?", Toast.LENGTH_LONG).show()
          log(e.toString())
          return@init
        }
        balances.putAll(MDS.getBalances(confirmations = 0).associateBy { it.tokenId })
        tokens.putAll(MDS.getTokens().associateBy { it.tokenId })
        createDB()
        channels.addAll(getChannels(status = "OPEN"))
        channels.forEach { channel ->
          channelKey(channel.my.keys, channel.tokenId).subscribe()
        }
        inited = true
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
        blockNumber = msg.jsonObject["data"]!!.jsonObject["txpow"]!!.jsonObject["header"]!!.jsonObject["block"]!!.jsonPrimitive.content.toInt()
        if (multisigScriptAddress.isNotEmpty()) {
          val newBalances = MDS.getBalances(multisigScriptAddress, confirmations = 0)
          if (newBalances.any { it.confirmed > ZERO } && multisigScriptBalances.none { it.confirmed > ZERO }) {
            setChannelOpen(multisigScriptAddress)
          }
          multisigScriptBalances.clear()
          multisigScriptBalances.addAll(newBalances)
        }
        if (eltooScriptAddress.isNotEmpty()) {
          eltooScriptCoins.put(eltooScriptAddress, MDS.getCoins(address = eltooScriptAddress))
        }
      }
    }
  }
}
