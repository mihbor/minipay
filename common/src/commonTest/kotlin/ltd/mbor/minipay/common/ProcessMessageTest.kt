package ltd.mbor.minipay.common

import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.resources.importTx
import kotlin.test.Test

class ProcessMessageTest {
  @Test
  fun process_update_ack() = runTest {
    //given
    val channels = mutableListOf(openChannel)
    val mds = SimulatedMDS().willReturn(importTx.createAndImportSignedTx).willReturn(importTx.createAndImportSignedTx)
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage, channels, mutableListOf())
    //when
    channelService.processMessage(importTx.updateAck, { _, _ -> }, {}, { 2 }, {})
    //then
    
  }
}