package ltd.mbor.minipay.common

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import ltd.mbor.minipay.common.model.Channel

object SimulatedStorage: ChannelStorage {
  var channels = mutableListOf<Channel>()
  var insertChannelId = uuid4()

  fun getChannelsWillReturn(newChannels: List<Channel>): SimulatedStorage {
    channels = newChannels.toMutableList()
    return this
  }

  fun insertChannelWillReturn(id: Uuid): SimulatedStorage {
    insertChannelId = id
    return this
  }

  override suspend fun createDB() {
    TODO("Not yet implemented")
  }
  
  override suspend fun getChannel(eltooAddress: String): Channel? {
    TODO("Not yet implemented")
  }

  override suspend fun getChannel(myKeys: Channel.Keys): Channel? {
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
    return channel.copy(triggerTx = triggerTx, settlementTx = settlementTx)
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
    theirAddress: String,
    maximaPK: String?
  ) = Channel(
    id = insertChannelId,
    sequenceNumber = 0,
    status = "OFFERED",
    tokenId = tokenId,
    my = Channel.Side(myAddress, myBalance, myKeys),
    their = Channel.Side(theirAddress, theirBalance, theirKeys),
    triggerTx = signedTriggerTx,
    settlementTx = signedSettlementTx,
    timeLock = timeLock,
    eltooAddress = eltooScriptAddress,
    multiSigAddress = multisigScriptAddress,
    updatedAt = Clock.System.now(),
    maximaPK = maximaPK
  )
}