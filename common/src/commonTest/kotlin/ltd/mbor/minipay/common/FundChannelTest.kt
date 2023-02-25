package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.model.Channel
import ltd.mbor.minipay.common.resources.address
import kotlin.test.Test
import kotlin.test.assertEquals

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
    channelService.prepareFundChannel(keys, keys, "address", ONE, ONE, "0x00", 10, "multisig", "eltoo") { event, channel -> events.add(event to channel) }
    //then
    assertEquals(1, transport.published.size)
  }
}