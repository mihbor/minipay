package ltd.mbor.minipay.common

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.JoinChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.resources.coinexport
import ltd.mbor.minipay.common.resources.importTx
import ltd.mbor.minipay.common.resources.scripts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class JoinChannelTest {
  @Test
  fun joinChannel() = runTest {
    //given
    val mds = SimulatedMDS()
      .willReturn(importTx.createAndImportSignedTx)
      .willReturn("""["TODO", "sign"]""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
      .willReturn(importTx.createAndImportSignedTx)
      .willReturn("""["TODO", "sign"]""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
      .willReturn(importTx.createAndImportSignedTx)
      .willReturnCoins(listOf(aCoin))
      .willReturn("""{"TODO": "fundingTx"}""")
      .willReturn(scripts.scripts109)
      .willReturn("""["TODO", "sign"]""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
      .willReturn(coinexport.coinexport)
    val storage = SimulatedStorage.insertChannelWillReturn(uuid4())
    val transport = SimulatedTransport()
    val channelService = ChannelService(mds, storage, transport, mutableListOf(), mutableListOf())
    val events = mutableListOf<Pair<JoinChannelEvent, Channel?>>()
    //when
    val channel = channelService.joinChannel(keys, keys, "my address", ONE, "0x00", 10, "multisig", "eltoo", "triggerTx", "settlementTx", "fundingTx") { event, channel -> events.add(event to channel) }
    //then
    assertNotNull(channel)
    assertEquals(1, transport.published.size)
    val eventsIterator = events.iterator()
    assertEquals(TRIGGER_TX_SIGNED to null, eventsIterator.next())
    assertEquals(SETTLEMENT_TX_SIGNED to null, eventsIterator.next())
    assertEquals(CHANNEL_PERSISTED to channel, eventsIterator.next())
    assertEquals(CHANNEL_PUBLISHED to channel, eventsIterator.next())
    assertEquals("my address", channel.my.address)
    assertEquals(keys, channel.my.keys)
    assertEquals(ONE, channel.my.balance)
    assertEquals("0xB13D03D7FAC25552491F8E1C04120FEA67700DFB5AB576CA7DDED59D35F93A96", channel.their.address)
    assertEquals(keys, channel.their.keys)
    assertEquals(ZERO, channel.their.balance)
    assertEquals(10, channel.timeLock)
    assertEquals("multisig", channel.multiSigAddress)
    assertEquals("eltoo", channel.eltooAddress)
  }
}