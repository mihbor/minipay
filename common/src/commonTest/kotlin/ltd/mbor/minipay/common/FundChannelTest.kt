package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.TEN
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.FundChannelEvent.*
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.resources.address
import ltd.mbor.minipay.common.resources.coinimport
import ltd.mbor.minipay.common.resources.importTx
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FundChannelTest {
  @Test
  fun prepareFundChannel() = runTest {
    //given
    val mds = SimulatedMDS()
      .willReturn(address.getAddress)
      .willReturnCoins(listOf(aCoin))
      .willReturn("""{"TODO": "fundingTx"}""")
      .willReturn("""["TODO", "signFloatingTx"]""")
      .willReturn("""["TODO", "signFloatingTx"]""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
      .willReturn("""{"response":{"data": "TODO: exportTx"}}""")
    val storage = SimulatedStorage.insertChannelWillReturn(42)
    val transport = SimulatedTransport()
    val channelService = ChannelService(mds, storage, transport, mutableListOf(), mutableListOf())
    val events = mutableListOf<Pair<FundChannelEvent, Channel?>>()
    //when
    val channel = channelService.prepareFundChannel(keys, keys, "their address", ONE, TEN, "0x00", 10, "multisig", "eltoo") { event, channel -> events.add(event to channel) }
    //then
    assertNotNull(channel)
    assertEquals(1, transport.published.size)
    val eventsIterator = events.iterator()
    assertEquals(FUNDING_TX_CREATED to null, eventsIterator.next())
    assertEquals(TRIGGER_TX_SIGNED to null, eventsIterator.next())
    assertEquals(SETTLEMENT_TX_SIGNED to null, eventsIterator.next())
    assertEquals(CHANNEL_PERSISTED to channel, eventsIterator.next())
    assertEquals(CHANNEL_PUBLISHED to channel, eventsIterator.next())
    assertEquals("0xB4A680430A9808AFA98D9F7E3398750AA71DD88E1A815D87C4FCC2A48C0A57D8", channel.my.address)
    assertEquals(keys, channel.my.keys)
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
      .willReturn(importTx.createAndImportSignedTx)
      .willReturn(importTx.createAndImportSignedTx)
      .willReturn(importTx.createAndImportSignedTx)
      .willReturn(coinimport.success)
      .willReturn(address.getAddress) // TODO: should be newscript
      .willReturn("""[{"command": "txnpost", "status": "true", "TODO": "postFundingTx"}]""")
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