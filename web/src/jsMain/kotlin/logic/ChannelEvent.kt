package logic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minipay.common.Channel

interface ChannelEvent {
  val channel: Channel
  val updateTxId: Int
  val settleTxId: Int
  val sequenceNumber: Int
  val channelBalance: Pair<BigDecimal, BigDecimal>
}

data class PaymentRequestReceived(
  override val channel: Channel,
  override val updateTxId: Int,
  override val settleTxId: Int,
  override val sequenceNumber: Int,
  override val channelBalance: Pair<BigDecimal, BigDecimal>,
) : ChannelEvent

data class PaymentRequestSent(
  override val channel: Channel,
  override val updateTxId: Int,
  override val settleTxId: Int,
  override val sequenceNumber: Int,
  override val channelBalance: Pair<BigDecimal, BigDecimal>,
) : ChannelEvent
