package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.Transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val keys = Channel.Keys("trigger", "update", "settle")
val offeredChannel = (Channel(1, 0, "OFFERED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val openChannel = (Channel(2, 0, "OPEN", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val triggeredChannel = (Channel(3, 0, "TRIGGERED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val updatedChannel = (Channel(4, 0, "UPDATED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val settledChannel = (Channel(5, 0, "SETTLED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val aCoin = Coin(
  address = "0x01234",
  miniAddress = "MxABCD",
  amount = BigDecimal.ONE,
  tokenAmount = BigDecimal.ONE,
  coinId = "0x012345",
  storeState = false,
  tokenId = "0x00",
  token = null,
  _created = "123",
  state = emptyList()
)
val aTransaction = Transaction(
  inputs = listOf(aCoin),
  outputs = listOf(aCoin),
  state = emptyList(),
  transactionId = "123",
  header = Transaction.Header(
    "123",
    "123"
  )
)

class ReloadChannelsTest {
  @Test
  fun reloadChannelsOffered() = runTest {
    //given
    val channels = mutableListOf(offeredChannel)
    val expected = listOf(offeredChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList())
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
  
  @Test
  fun reloadChannelsOfferedGoesOpen() = runTest {
    //given
    val channels = mutableListOf(offeredChannel)
    val expected = listOf(offeredChannel.copy(status = "OPEN"))
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin))
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
  
  @Test
  fun reloadChannelsOpen() = runTest {
    //given
    val channels = mutableListOf(openChannel)
    val expected = listOf(openChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList())
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsOpenGoesTriggered() = runTest {
    //given
    val channels = mutableListOf(openChannel)
    val expected = listOf(openChannel.copy(status = "TRIGGERED"))
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin))
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertEquals(listOf(aCoin), eltooCoins[offeredChannel.eltooAddress])
  }
  
  @Test
  fun reloadChannelsTriggeredWithEltooCoins() = runTest {
    //given
    val channels = mutableListOf(triggeredChannel)
    val expected = listOf(triggeredChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin)).willReturnTransactions(emptyList())
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertEquals(listOf(aCoin), eltooCoins[offeredChannel.eltooAddress])
  }
  
  @Test
  fun reloadChannelsTriggeredWithoutEltooCoins() = runTest {
    //given
    val channels = mutableListOf(triggeredChannel)
    val expected = listOf(triggeredChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(emptyList())
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsTriggeredWithoutEltooCoinsWithTransaction() = runTest {
    //given
    val channels = mutableListOf(triggeredChannel)
    val expected = listOf(triggeredChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(listOf(aTransaction))
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsTriggeredGoesSettled() = runTest {
    //given
    val channels = mutableListOf(triggeredChannel)
    val expected = listOf(triggeredChannel.copy(status = "SETTLED"))
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val transactionFromEltoo = aTransaction.copy(inputs = aTransaction.inputs.map { it.copy(address = triggeredChannel.eltooAddress) })
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(listOf(transactionFromEltoo))
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(mds, storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }

  @Test
  fun reloadChannelsSettled() = runTest {
    //given
    val channels = mutableListOf(settledChannel)
    val expected = listOf(settledChannel)
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val storage = SimulatedStorage.willReturn(channels)
    val channelService = ChannelService(SimulatedMDS(), storage)
    //when
    channelService.reloadChannels(channels, eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
}
