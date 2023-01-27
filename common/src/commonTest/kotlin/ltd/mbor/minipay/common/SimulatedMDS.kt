package ltd.mbor.minipay.common

import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.MDSInterface
import ltd.mbor.minimak.json

object SimulatedMDS: MDSInterface {
  var payloads: MutableList<JsonElement?> = mutableListOf(null)
  var iterator = payloads.iterator()
  var capturedCommands = mutableListOf<String>()
  var capturedQueries = mutableListOf<String>()
  
  fun willReturn(payload: String?): SimulatedMDS {
    payloads = mutableListOf(payload?.let(json::parseToJsonElement))
    iterator = payloads.iterator()
    return this
  }
  
  fun then(payload: String?): SimulatedMDS {
    payloads += payload?.let(json::parseToJsonElement)
    return this
  }
  
  override suspend fun cmd(command: String): JsonElement? {
    capturedCommands += command
    return iterator.next()
  }
  
  override suspend fun sql(query: String): JsonElement? {
    capturedQueries += query
    return iterator.next()
  }
}
