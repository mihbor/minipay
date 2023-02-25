package ltd.mbor.minipay.common.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface Transport {
  fun subscribe(id: String, from: Instant? = null): Flow<String>
  suspend fun publish(id: String, content: String)
}