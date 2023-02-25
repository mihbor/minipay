package ltd.mbor.minipay.common

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ltd.mbor.minipay.common.transport.Transport

class SimulatedTransport(var published: MutableList<Pair<String, String>> = mutableListOf()): Transport {
  override fun subscribe(id: String, from: Instant?): Flow<String> {
    TODO("Not yet implemented")
  }

  override suspend fun publish(id: String, content: String) {
    published += content to id
  }
}