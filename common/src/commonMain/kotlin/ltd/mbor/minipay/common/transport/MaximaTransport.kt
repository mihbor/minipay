package ltd.mbor.minipay.common.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ltd.mbor.minimak.Contact
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.sendMessage

const val APP = "minipay"

class MaximaTransport(val contact: Contact): Transport {
  override fun subscribe(id: String, from: Instant?): Flow<String> {
    TODO("Not yet implemented")
  }

  override suspend fun publish(id: String, content: String) {
    MDS.sendMessage(APP, contact.publicKey, "$id:$content")
  }
}