package ltd.mbor.minipay.common

import kotlinx.coroutines.MainScope
import kotlin.random.Random

fun newTxId() = Random.nextInt(1_000_000_000)

val scope = MainScope()
