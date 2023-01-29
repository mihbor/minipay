package ltd.mbor.minipay.common

import ltd.mbor.minimak.*

class ChannelService(
  val mds: MdsApi,
  val storage: ChannelStorage,
) {
  suspend fun reloadChannels(channels: MutableList<Channel>, eltooScriptCoins: MutableMap<String, List<Coin>>) {
    val newChannels = storage.getChannels().map { channel ->
      when (channel.status) {
        "OFFERED" -> {
          val multiSigCoins = mds.getCoins(address = channel.multiSigAddress)
          if (multiSigCoins.isNotEmpty()) storage.updateChannelStatus(channel, "OPEN")
          else channel
        }
      
        in setOf("OPEN", "TRIGGERED", "UPDATED") -> {
          val eltooCoins = mds.getCoins(address = channel.eltooAddress)
          eltooScriptCoins[channel.eltooAddress] = eltooCoins
          if (channel.status == "OPEN" && eltooCoins.isNotEmpty()) storage.updateChannelStatus(channel, "TRIGGERED")
          else if (channel.status in listOf("TRIGGERED", "UPDATED") && eltooCoins.isEmpty()) {
            val anyTransactionsFromEltoo = mds.getTransactions(channel.eltooAddress)
              ?.any { it.inputs.any { it.address == channel.eltooAddress } } ?: false
            if (anyTransactionsFromEltoo) storage.updateChannelStatus(channel, "SETTLED")
            else channel
          } else channel
        }
      
        else -> channel
      }
    }
    channels.clear()
    channels.addAll(newChannels)
  }
}

val channelService = ChannelService(MDS, storage)
