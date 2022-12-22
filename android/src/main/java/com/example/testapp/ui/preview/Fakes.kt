package com.example.testapp.ui.preview

import com.example.testapp.Channel
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.Balance

val fakeChannel = Channel(
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
  eltooAddress = "Mx123",
  updatedAt = 123
)

val fakeBalances = mapOf(
  "0x00" to Balance("0x00", JsonObject(mapOf("name" to JsonPrimitive("Minima"))), ONE, ZERO, ONE, "1")
)