package ltd.mbor.minipay.common

import kotlinx.coroutines.test.runTest
import ltd.mbor.minimak.Coin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReloadChannelsTest {
  @Test
  fun reloadChannelsOffered() = runTest {
    //given
    val channels = mutableMapOf(offeredChannel.id to offeredChannel)
    val expected = listOf(offeredChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList())
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
  
  @Test
  fun reloadChannelsOfferedGoesOpen() = runTest {
    //given
    val channels = mutableMapOf(offeredChannel.id to offeredChannel)
    val expected = listOf(offeredChannel.copy(status = "OPEN")).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin))
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
  
  @Test
  fun reloadChannelsOpen() = runTest {
    //given
    val channels = mutableMapOf(openChannel.id to openChannel)
    val expected = listOf(openChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList())
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsOpenGoesTriggered() = runTest {
    //given
    val channels = mutableMapOf(openChannel.id to openChannel)
    val expected = listOf(openChannel.copy(status = "TRIGGERED")).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin))
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertEquals(listOf(aCoin), eltooCoins[offeredChannel.eltooAddress])
  }
  
  @Test
  fun reloadChannelsTriggeredWithEltooCoins() = runTest {
    //given
    val channels = mutableMapOf(triggeredChannel.id to triggeredChannel)
    val expected = listOf(triggeredChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(listOf(aCoin)).willReturnTransactions(emptyList())
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertEquals(listOf(aCoin), eltooCoins[offeredChannel.eltooAddress])
  }
  
  @Test
  fun reloadChannelsTriggeredWithoutEltooCoins() = runTest {
    //given
    val channels = mutableMapOf(triggeredChannel.id to triggeredChannel)
    val expected = listOf(triggeredChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(emptyList())
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsTriggeredWithoutEltooCoinsWithTransaction() = runTest {
    //given
    val channels = mutableMapOf(triggeredChannel.id to triggeredChannel)
    val expected = listOf(triggeredChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(listOf(aTransaction))
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }
  
  @Test
  fun reloadChannelsTriggeredGoesSettled() = runTest {
    //given
    val channels = mutableMapOf(triggeredChannel.id to triggeredChannel)
    val expected = listOf(triggeredChannel.copy(status = "SETTLED")).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val transactionFromEltoo = aTransaction.copy(inputs = aTransaction.inputs.map { it.copy(address = triggeredChannel.eltooAddress) })
    val mds = SimulatedMDS().willReturnCoins(emptyList()).willReturnTransactions(listOf(transactionFromEltoo))
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(mds, storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(checkNotNull(eltooCoins[offeredChannel.eltooAddress]).isEmpty())
  }

  @Test
  fun reloadChannelsSettled() = runTest {
    //given
    val channels = mutableMapOf(settledChannel.id to settledChannel)
    val expected = listOf(settledChannel).associateBy { it.id }
    val eltooCoins = mutableMapOf<String, List<Coin>>()
    val storage = SimulatedStorage.getChannelsWillReturn(channels.values.toList())
    val channelService = ChannelService(SimulatedMDS(), storage, SimulatedTransport(), channels, mutableListOf())
    //when
    channelService.reloadChannels(eltooCoins)
    //then
    assertEquals(expected, channels)
    assertTrue(eltooCoins.isEmpty())
  }
}
