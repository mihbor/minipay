package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.coroutines.test.runTest
import ltd.mbor.minipay.common.resources.three_new_keys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ChannelsTest {
  @Test
  fun channelKey() {
    //given
    val keys = Channel.Keys(
      trigger = "0x51433661894E48B0082D4B0FF0BF9776550FA138F2E9EAA1DC9822BD9AE83FCF",
      update = "0x790FADB41511DF55B0CCD4B2900C7A8500E5A534E2B95DB233387CE48D28364D",
      settle = "0x12E952C9F4AE5911D74E8FC0EF287198121E18204A783BEC7DDF853DE76F043A"
    )
    val tokenId = "0x00"
    //when
    val result = channelKey(keys, tokenId)
    //then
    assertEquals("0x51433661894E48B0082D4B0FF0BF9776550FA138F2E9EAA1DC9822BD9AE83FCF;0x790FADB41511DF55B0CCD4B2900C7A8500E5A534E2B95DB233387CE48D28364D;0x12E952C9F4AE5911D74E8FC0EF287198121E18204A783BEC7DDF853DE76F043A;0x00", result)
  }

  @Test
  fun newKeys() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(three_new_keys)
    //when
    val (first, second, third) = mds.newKeys(3)
    //then
    assertEquals("0x68F53E7C16BCA4F81A8186CFFD858D4A8E5FABA7237BF0F303088946AEEB1C10", first)
    assertEquals("0x80FA8FD2DEF91DBAD3993D8FAF58B62517FDFE6675F60B1CB1417F54A7B58AD4", second)
    assertEquals("0x8C2BECF43D67E4382190E123FA48AC3D35E94E9FF793FBC7410B583BDF5C612B", third)
  }

  @Test
  fun signFloatingTx_filters_out_zero_outputs() = runTest {
    //given
    val myKey = "abc"
    val sourceAddress = "0x01234"
    val tokenId = "0x00"
    val amountToAddress1 = ONE to "something"
    val amountToAddress2 = ZERO to "nothing"
    val mds = SimulatedMDS().willReturn("[]")
    //when
    mds.signFloatingTx(myKey, sourceAddress, tokenId, states = emptyMap(), amountToAddress1, amountToAddress2)
    //then
    assertFalse(mds.capturedCommands.first().contains("nothing"))
  }
}