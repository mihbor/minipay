package ltd.mbor.minipay.common

import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.resources.importTx
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessMessageTest {
  @Test
  fun process_update_ack() = runTest {
    //given
    val channels = mutableListOf(openChannel)
    val mds = SimulatedMDS().willReturn(importTx.createAndImportSignedTx).willReturn(importTx.createAndImportSignedTx)
    val storage = SimulatedStorage.getChannelsWillReturn(channels)
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    var channel: Channel? = null
    var isAck: Boolean? = null
    //when
    channelService.processMessage(importTx.updateAck, { c, ack ->
      channel = c
      isAck = ack
    }, {}, { openChannel.id }, {})
    //then
    assertTrue(checkNotNull(isAck))
    assertEquals(openChannel, channel)
  }
}