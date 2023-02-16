package ltd.mbor.minipay.common

import com.ionspin.kotlin.bignum.decimal.BigDecimal

interface ChannelEvent {
  val channel: Channel
  val updateTxId: Int
  val settleTxId: Int
  val sequenceNumber: Int
  val channelBalance: Pair<BigDecimal, BigDecimal>
  val transport: Transport
}

data class PaymentRequestReceived(
  override val channel: Channel,
  override val updateTxId: Int,
  override val settleTxId: Int,
  override val sequenceNumber: Int,
  override val channelBalance: Pair<BigDecimal, BigDecimal>,
  override val transport: Transport,
) : ChannelEvent

data class PaymentRequestSent(
  override val channel: Channel,
  override val updateTxId: Int,
  override val settleTxId: Int,
  override val sequenceNumber: Int,
  override val channelBalance: Pair<BigDecimal, BigDecimal>,
  override val transport: Transport
) : ChannelEvent
