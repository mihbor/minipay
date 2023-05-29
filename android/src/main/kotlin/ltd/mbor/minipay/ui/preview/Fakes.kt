package ltd.mbor.minipay.ui.preview

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.*
import ltd.mbor.minipay.common.model.*
import ltd.mbor.minipay.common.model.Transport.*

val fakeChannelOpen = Channel(
  id = uuid4(),
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

val fakeChannelTriggered = fakeChannelOpen.copy(status = "TRIGGERED", sequenceNumber = 3, eltooAddress = "0x999", updateTx = "abc")

val fakeMinimaChannel = Channel(
  id = uuid4(),
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

val fakeEltooCoins = mutableMapOf("0x999" to listOf(Coin(address = "", miniAddress = "", amount = BigDecimal.ONE, coinId = "", storeState = true, tokenId = "0x00", _created = "100", token = null, state = emptyList())))

val previewPrefs = Prefs("uid123", "localhost", 9004)

val previewKeys = Channel.Keys("a", "b", "c")

val previewBalances = listOf(
  Balance("0x00", JsonNull, ONE, ONE, ONE, ONE, "1"),
  Balance("0x01234567890", JsonPrimitive("test token"), ONE, ONE, ONE, ONE, "1"),
).associateBy { it.tokenId }

val previewTokens = listOf(
  Token("0x00", JsonNull, ONE, 1, null, null, null, JsonNull),
  Token("0x01234567890", JsonPrimitive("test token"), ONE, 1, null, null, null, JsonNull),
  Token("0x0999", JsonPrimitive("test2"), ONE, 1, null, null, null, JsonNull),
).associateBy { it.tokenId }

val previewInvite = ChannelInvite(
  tokenId = "0x00",
  address = "0x123",
  balance = ONE,
  keys = Channel.Keys("0x111", "0x222", "0x333")
)

val previewEvents = mutableListOf(
  PaymentRequestSent(fakeChannelOpen, 1, 2, 3, ZERO to ONE, NFC),
  PaymentRequestSent(fakeChannelOpen, 1, 2, 3, ZERO to ONE, FIREBASE),
  PaymentRequestReceived(fakeChannelOpen, 1, 2, 3, ZERO to ONE, NFC),
  PaymentRequestReceived(fakeChannelOpen, 1, 2, 3, ZERO to ONE, FIREBASE),
  ChannelInviteReceived(previewInvite, MAXIMA)
)

val previewMaximaInfo = MaximaInfo(
  logs = false,
  name = "Alice",
  publicKey = "0x30819F300D06092A864886F70D010101050003818D0030818902818100ABDB20647C2466FBFB7B648FFBFAB799E40E8EB2975264033D1166D8D08A0FD8B7D818B60AFAF45D29DFFD762EF96C0584DA1A9213A7CB6E3339032FAD4794B033C94AFF3D3C45B8ACAE9BEE226B281D34A6E9779B379D1FA54CC7634A0BFEC6258A2F47DB4DC4A25A3F8A4B4601A398EA7C2E3FFBF6F5FC8F35B4103773A7730203010001",
  staticMls = false,
  mls = "MxG18HGG6FJ038614Y8CW46US6G20810K0070CD00Z83282G60G1DEE5WAUAA01612RB3P6JM44DHTSDNT8G2E8ZFEP4AMHCTG6C5VSWQGHD6GDJ6YQ5031YMMAVR7K2404QSYP1CC6RD7GJT95P81QPCZCKVV5ARBQ21YQ55JY2S93TT8Y6FCWCEK94G5S37MG82ENJZSDS054KRMJTNEZ4NJSBM2D8VB254W9QHSB0A2W00GGJC3B4MSE0F3VRC10608006EBQHYM@172.25.144.1:11001",
  localIdentity = "MxG18HGG6FJ038614Y8CW46US6G20810K0070CD00Z83282G60G1DEE5WAUAA01612RB3P6JM44DHTSDNT8G2E8ZFEP4AMHCTG6C5VSWQGHD6GDJ6YQ5031YMMAVR7K2404QSYP1CC6RD7GJT95P81QPCZCKVV5ARBQ21YQ55JY2S93TT8Y6FCWCEK94G5S37MG82ENJZSDS054KRMJTNEZ4NJSBM2D8VB254W9QHSB0A2W00GGJC3B4MSE0F3VRC10608006EBQHYM@172.25.144.1:11001",
  p2pIdentity = "MxG18HGG6FJ038614Y8CW46US6G20810K0070CD00Z83282G60G1DEE5WAUAA01612RB3P6JM44DHTSDNT8G2E8ZFEP4AMHCTG6C5VSWQGHD6GDJ6YQ5031YMMAVR7K2404QSYP1CC6RD7GJT95P81QPCZCKVV5ARBQ21YQ55JY2S93TT8Y6FCWCEK94G5S37MG82ENJZSDS054KRMJTNEZ4NJSBM2D8VB254W9QHSB0A2W00GGJC3B4MSE0F3VRC10608006EBQHYM@172.25.144.1:11001",
  contact = "MxG18HGG6FJ038614Y8CW46US6G20810K0070CD00Z83282G60G1DEE5WAUAA01612RB3P6JM44DHTSDNT8G2E8ZFEP4AMHCTG6C5VSWQGHD6GDJ6YQ5031YMMAVR7K2404QSYP1CC6RD7GJT95P81QPCZCKVV5ARBQ21YQ55JY2S93TT8Y6FCWCEK94G5S37MG82ENJZSDS054KRMJTNEZ4NJSBM2D8VB254W9QHSB0A2W00GGJC3B4MSE0F3VRC10608006EBQHYM@172.25.144.1:11001",
  poll = 0
)

val alice = Contact(
  id = 1,
  publicKey = "0x0123",
  currentAddress = "0x0987",
  myAddress = "0x0000",
  lastSeen = 1234,
  date = "",
  _chainTip = "0",
  sameChain = true,
  extraData = ExtraData(
    name = "Alice",
    minimaAddress = "0x0987",
    _topBlock = "123",
    _checkBlock = "0",
    checkHash = "",
    mls = ""
  )
)
val bob = Contact(
  id = 2,
  publicKey = "0x0123",
  currentAddress = "0x0987",
  myAddress = "0x0000",
  lastSeen = 1234,
  date = "",
  _chainTip = "0",
  sameChain = false,
  extraData = ExtraData(
    name = "Bob",
    minimaAddress = "0x0987",
    _topBlock = "123",
    _checkBlock = "0",
    checkHash = "",
    mls = ""
  )
)
val previewContacts = listOf(alice, bob)
