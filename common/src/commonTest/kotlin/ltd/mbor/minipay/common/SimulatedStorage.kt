package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minipay.common.model.Channel

object SimulatedStorage: ChannelStorage {
  var channels = mutableListOf<Channel>()

  fun willReturn(newChannels: List<Channel>): SimulatedStorage {
    channels = newChannels.toMutableList()
    return this
  }

  override suspend fun createDB() {
    TODO("Not yet implemented")
  }
  
  override suspend fun getChannel(eltooAddress: String): Channel? {
    TODO("Not yet implemented")
  }
  
  override suspend fun getChannels(status: String?): List<Channel> {
    return channels
  }
  
  override suspend fun updateChannelStatus(channel: Channel, status: String): Channel {
    return channel.copy(status = status)
  }
  
  override suspend fun setChannelOpen(multisigAddress: String) {
    TODO("Not yet implemented")
  }
  
  override suspend fun updateChannel(channel: Channel, triggerTx: String, settlementTx: String): Channel {
    TODO("Not yet implemented")
  }
  
  override suspend fun updateChannel(channel: Channel, channelBalance: Pair<BigDecimal, BigDecimal>, sequenceNumber: Int, updateTx: String, settlementTx: String): Channel {
    return channel
  }
  
  override suspend fun insertChannel(
    tokenId: String,
    myBalance: BigDecimal,
    theirBalance: BigDecimal,
    myKeys: Channel.Keys,
    theirKeys: Channel.Keys,
    signedTriggerTx: String,
    signedSettlementTx: String,
    timeLock: Int,
    multisigScriptAddress: String,
    eltooScriptAddress: String,
    myAddress: String,
    otherAddress: String
  ): Int {
    TODO("Not yet implemented")
  }
}