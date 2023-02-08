package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.Balance
import ltd.mbor.minimak.getBalances
import ltd.mbor.minipay.common.resources.balance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AdapterTest {

  @Test
  fun zero_balance() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(balance.zero)
    //when
    val result = mds.getBalances()
    //then
    assertEquals(
      listOf(
        Balance(
        tokenId = "0x00",
        _token = JsonPrimitive("Minima"),
        total = "1000000000".toBigDecimal(),
        confirmed = BigDecimal.ZERO,
        unconfirmed = BigDecimal.ZERO,
        sendable = BigDecimal.ZERO,
        _coins = "0"
      )
      ),
      result
    )
    assertEquals("Minima", result.first().tokenName)
    assertNull(result.first().tokenUrl)
    assertEquals(0, result.first().coins)
  }

}