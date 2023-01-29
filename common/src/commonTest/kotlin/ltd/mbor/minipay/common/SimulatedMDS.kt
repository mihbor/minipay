package ltd.mbor.minipay.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.MdsApi
import ltd.mbor.minimak.Transaction
import ltd.mbor.minimak.json

class SimulatedMDS: MdsApi {
  var payloads: MutableList<JsonElement?> = mutableListOf()
  var iterator = payloads.iterator()
  var capturedCommands = mutableListOf<String>()
  var capturedQueries = mutableListOf<String>()
  
  fun willReturn(payload: String?): SimulatedMDS {
    payloads = mutableListOf(payload?.let(json::parseToJsonElement))
    iterator = payloads.iterator()
    return this
  }
  
  fun willReturnCoins(coins: List<Coin>): SimulatedMDS {
    payloads += JsonObject(mapOf("response" to json.encodeToJsonElement(coins)))
    iterator = payloads.iterator()
    return this
  }
  
  fun willReturnTransactions(transactions: List<Transaction>): SimulatedMDS {
    payloads += JsonObject(mapOf(
      "response" to JsonArray(transactions.map {
        JsonObject(mapOf(
          "body" to JsonObject(mapOf(
            "txn" to json.encodeToJsonElement(it)
          )),
          "header" to json.encodeToJsonElement(it.header)
        ))
      })
    ))
    iterator = payloads.iterator()
    return this
  }
  
  override var logging: Boolean = false
  
  override suspend fun cmd(command: String): JsonElement? {
    capturedCommands += command
    return iterator.next()
  }
  
  override suspend fun sql(query: String): JsonElement? {
    capturedQueries += query
    return iterator.next()
  }
}
