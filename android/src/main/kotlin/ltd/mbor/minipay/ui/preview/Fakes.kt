package ltd.mbor.minipay.ui.preview

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.Token
import ltd.mbor.minipay.common.Channel
import ltd.mbor.minipay.logic.PaymentRequestReceived
import ltd.mbor.minipay.logic.PaymentRequestSent

val fakeChannel = Channel(
  id = 1,
  sequenceNumber = 0,
  status = "OPEN",
  tokenId = "0x01234567890",
  my = Channel.Side(
    balance = ONE,
    address = "Mx0123456789",
    keys = Channel.Keys(
      trigger = "0x123",
      update = "0x123",
      settle = "0x123",
    )
  ),
  their = Channel.Side(
    balance = ONE,
    address = "Mx1234567890",
    keys = Channel.Keys(
      trigger = "0x123",
      update = "0x123",
      settle = "0x123",
    )
  ),
  triggerTx = "",
  updateTx = "",
  settlementTx = "",
  timeLock = 10,
  eltooAddress = "0x0123",
  multiSigAddress = "0x2345",
  updatedAt = Instant.fromEpochMilliseconds(123)
)

val fakeTriggeredChannel = fakeChannel.copy(status = "TRIGGERED")

val fakeMinimaChannel = Channel(
  id = 1,
  sequenceNumber = 0,
  status = "OPEN",
  tokenId = "0x00",
  my = Channel.Side(
    balance = ONE,
    address = "Mx0123456789",
    keys = Channel.Keys(
      trigger = "0x123",
      update = "0x123",
      settle = "0x123",
    )
  ),
  their = Channel.Side(
    balance = ONE,
    address = "Mx1234567890",
    keys = Channel.Keys(
      trigger = "0x123",
      update = "0x123",
      settle = "0x123",
    )
  ),
  triggerTx = "",
  updateTx = "",
  settlementTx = "",
  timeLock = 10,
  eltooAddress = "0x0123",
  multiSigAddress = "0x2345",
  updatedAt = Instant.fromEpochMilliseconds(123)
)

val fakeCoin = Coin(
  address = "0x01234",
  miniAddress = "MxABCD",
  amount = ONE,
  tokenAmount = ONE,
  coinId = "0x012345",
  storeState = false,
  tokenId = "0x00",
  token = null,
  _created = "123",
  state = emptyList()
)

val fakeBalances = mapOf(
  "0x00" to Balance("0x00", JsonObject(mapOf("name" to JsonPrimitive("Minima"))), ONE, ONE, ZERO, ONE, "1")
)

val fakeEltooCoins = mutableMapOf("Mx999" to listOf(Coin(address = "", miniAddress = "", amount = BigDecimal.ONE, coinId = "", storeState = true, tokenId = "0x00", _created = "100", token = null, state = emptyList())))

val previewBalances = listOf(
  Balance("0x00", JsonNull, ONE, ONE, ONE, ONE, "1"),
  Balance("0x01234567890", JsonPrimitive("test token"), ONE, ONE, ONE, ONE, "1"),
).associateBy { it.tokenId }

val previewTokens = listOf(
  Token("0x00", JsonNull, ONE, 1, null, null, null, JsonNull),
  Token("0x01234567890", JsonPrimitive("test token"), ONE, 1, null, null, null, JsonNull),
  Token("0x0999", JsonPrimitive("test2"), ONE, 1, null, null, null, JsonNull),
).associateBy { it.tokenId }

val previewEvents = mutableListOf(
  PaymentRequestSent(fakeChannel, 1, 2, 3, ZERO to ONE, true),
  PaymentRequestSent(fakeChannel, 1, 2, 3, ZERO to ONE, false),
  PaymentRequestReceived(fakeChannel, 1, 2, 3, ZERO to ONE, true),
  PaymentRequestReceived(fakeChannel, 1, 2, 3, ZERO to ONE, false),
)