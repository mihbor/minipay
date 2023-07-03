package ltd.mbor.minipay.common.model

import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant

data class Channel(
  val id: Uuid,
  val sequenceNumber: Int = 0,
  val status: String,
  val tokenId: String,
  val my: Side,
  val their: Side,
  val triggerTx: String,
  val updateTx: String = "",
  val settlementTx: String,
  val timeLock: Int,
  val eltooAddress: String,
  val multiSigAddress: String,
  val updatedAt: Instant,
  val maximaPK: String? = null
) {
  data class Side(
    val address: String,
    val balance: BigDecimal,
    val keys: Keys
  )
  data class Keys(
    val trigger: String,
    val update: String,
    val settle: String
  ) {
    companion object {
      val EMPTY = Keys("", "", "")
    }
  }
}

data class ChannelInvite(
  val tokenId: String,
  val address: String,
  val balance: BigDecimal,
  val keys: Channel.Keys,
  val maximaPK: String?
) {
  companion object{
    val EMPTY = ChannelInvite(
      tokenId = "0x00",
      address = "",
      balance = BigDecimal.ZERO,
      Channel.Keys("", "", ""),
      null
    )
  }
}
