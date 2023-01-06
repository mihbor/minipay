package ltd.mbor.minipay.common

import kotlin.random.Random

fun newTxId() = Random.nextInt(1_000_000_000)
