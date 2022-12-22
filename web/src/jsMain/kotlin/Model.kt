import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class Channel(
  val id: Int,
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
  val updatedAt: Long
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
  )
}
