package ltd.mbor.minipay.common

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.RequestChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.resources.requestChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RequestChannelTest {
  @Test
  fun joinChannel() = runTest {
    //given
    val mds = SimulatedMDS()
      .willReturn(requestChannel.importTriggerTx)
      .willReturn(requestChannel.signTriggerTx)
      .willReturn(requestChannel.exportTriggerTx)
      .willReturn(requestChannel.importSettleTx)
      .willReturn(requestChannel.signSettleTx)
      .willReturn(requestChannel.exportSettleTx)
      .willReturn(requestChannel.importFundingTx)
      .willReturn(requestChannel.coinsForFunding)
      .willReturn(requestChannel.addressForChange)
      .willReturn(requestChannel.fundingTx)
      .willReturn(requestChannel.scriptsForFundingCoins)
      .willReturn(requestChannel.signFundingTx)
      .willReturn(requestChannel.exportFundingTx)
      .willReturn(requestChannel.exportFundingCoin)
    val storage = SimulatedStorage.insertChannelWillReturn(uuid4())
    val transport = SimulatedTransport()
    val channelService = ChannelService(mds, storage, transport, mutableListOf(), mutableListOf())
    val events = mutableListOf<Pair<RequestChannelEvent, Channel?>>()
    //when
    val channel = channelService.requestedChannelAccepted(keys, keys2, "my address", "their address", ONE, "0x00", 10, "multisig", "eltoo", "triggerTx", "settlementTx", "fundingTx", null) { event, channel -> events.add(event to channel) }
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
    assertEquals("their address", channel.their.address)
    assertEquals(keys2, channel.their.keys)
    assertEquals(50.toBigDecimal(), channel.their.balance)
    assertEquals(10, channel.timeLock)
    assertEquals("multisig", channel.multiSigAddress)
    assertEquals("eltoo", channel.eltooAddress)
  }
}