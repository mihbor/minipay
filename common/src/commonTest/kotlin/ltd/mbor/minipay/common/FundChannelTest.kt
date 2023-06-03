package ltd.mbor.minipay.common

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.TEN
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.resources.fundChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FundChannelTest {
  @Test
  fun prepareFundChannel() = runTest {
    //given
    val mds = SimulatedMDS()
      .willReturn(fundChannel.coins)
      .willReturn(fundChannel.getaddress4change)
      .willReturn(fundChannel.fundingTx)
      .willReturn(fundChannel.triggerTx)
      .willReturn(fundChannel.settleTx)
      .willReturn(fundChannel.exportTriggerTx)
      .willReturn(fundChannel.exportSettleTx)
      .willReturn(fundChannel.exportFundingTx)
    val storage = SimulatedStorage.insertChannelWillReturn(uuid4())
    val transport = SimulatedTransport()
    val channelService = ChannelService(mds, storage, transport, mutableListOf(), mutableListOf())
    val events = mutableListOf<Pair<FundChannelEvent, Channel?>>()
    //when
    val channel = channelService.prepareFundChannel(ChannelInvite("0x00", "their address", TEN, keys, null), keys2, "my address", ONE, 10, "multisig", "eltoo") { event, channel -> events.add(event to channel) }
    //then
    assertNotNull(channel)
    assertEquals(1, transport.published.size)
    assertEquals(channelKey(keys, "0x00"), transport.published.first().first)
    assertEquals(
      listOf("ACCEPTED", 10, keys2.trigger, keys2.update, keys2.settle, fundChannel.exportTriggerTxData, fundChannel.exportSettleTxData, fundChannel.exportFundingTxData, "my address").joinToString(";"),
      transport.published.first().second
    )
    val eventsIterator = events.iterator()
    assertEquals(FUNDING_TX_CREATED to null, eventsIterator.next())
    assertEquals(TRIGGER_TX_SIGNED to null, eventsIterator.next())
    assertEquals(SETTLEMENT_TX_SIGNED to null, eventsIterator.next())
    assertEquals(CHANNEL_PERSISTED to channel, eventsIterator.next())
    assertEquals(CHANNEL_PUBLISHED to channel, eventsIterator.next())
    assertEquals("my address", channel.my.address)
    assertEquals(keys2, channel.my.keys)
    assertEquals(ONE, channel.my.balance)
    assertEquals("their address", channel.their.address)
    assertEquals(keys, channel.their.keys)
    assertEquals(TEN, channel.their.balance)
    assertEquals(10, channel.timeLock)
    assertEquals("multisig", channel.multiSigAddress)
    assertEquals("eltoo", channel.eltooAddress)
  }

  @Test
  fun commitFundChannel() = runTest {
    //given
    val mds = SimulatedMDS()
      .willReturn(fundChannel.importTriggerTx)
      .willReturn(fundChannel.importSettleTx)
      .willReturn(fundChannel.importFundingTx)
      .willReturn(fundChannel.importFundingCoin)
      .willReturn(fundChannel.fundingCoinScript)
      .willReturn(fundChannel.postFundingTx)
    val storage = SimulatedStorage
    val transport = SimulatedTransport()
    val channelService = ChannelService(mds, storage, transport, mutableListOf(), mutableListOf())
    //when
    val channel = with(channelService) {
      offeredChannel.commitFund("triggerTx", "settlementTx", "fundingTx", listOf(aCoin.coinId), listOf(anAddress.script))
    }
    //then
    assertEquals("triggerTx", channel.triggerTx)
    assertEquals("settlementTx", channel.settlementTx)
  }
}