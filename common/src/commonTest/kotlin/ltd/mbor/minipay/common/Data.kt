package ltd.mbor.minipay.common

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import ltd.mbor.minimak.Address
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.Transaction
import ltd.mbor.minipay.common.model.Channel

val keys = Channel.Keys("trigger", "update", "settle")
val keys2 = Channel.Keys("trigger2", "update2", "settle2")
val offeredChannel = (Channel(uuid4(), "offered channel", 0, "OFFERED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val openChannel = (Channel(uuid4(), "open channel", 0, "OPEN", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val triggeredChannel = (Channel(uuid4(), "triggered channel", 0, "TRIGGERED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val updatedChannel = (Channel(uuid4(), "updated channel", 0, "UPDATED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
val settledChannel = (Channel(uuid4(), "settled channel", 0, "SETTLED", "0x00", Channel.Side("abc", BigDecimal.ONE, keys), Channel.Side("def", BigDecimal.ONE, keys), "triggerTx", "updateTx", "settleTx", 10, "eltoo", "multisig", Clock.System.now()))
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
val anAddress = Address(
  script = "RETURN SIGNEDBY(0xB3C8B10782B90C1820111CBE33E207BDF2B7D4C4FF004E5EF38F301847EB2ED8)",
  address = "0x3DD886A5FF072F44A576829B32019DB4DB98BF8E37FD819CA59798A3C2FDDBE9",
  miniAddress = "MxG081TR23ABVZ75T2AATK2JCP037DKRECBV3HNVM0PP9CNJ2HS5VERT4GUC94G",
  simple = true,
  default = true,
  publicKey = "0xB3C8B10782B90C1820111CBE33E207BDF2B7D4C4FF004E5EF38F301847EB2ED8",
  track = true
)
