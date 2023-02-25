package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.Transaction
import ltd.mbor.minipay.common.model.Channel

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
