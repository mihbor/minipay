package ui

import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.Color.red
import org.jetbrains.compose.web.css.LineStyle.Companion.Solid
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.TextInput

@Composable
fun DecimalNumberInput(
  value: BigDecimal? = null,
  min: BigDecimal? = null,
  max: BigDecimal? = null,
  disabled: Boolean = false,
  setValue: (BigDecimal?) -> Unit = {},
) {
  var text by remember { mutableStateOf(value?.toPlainString() ?: "") }
  var isValid by remember { mutableStateOf(value?.isBetween(min, max) ?: false) }
  value.takeUnless { it == text.toBigDecimalOrNull() || it == ZERO && text.isEmpty() }
    ?.let {
      text = it.toPlainString()
      isValid = true
    }
  TextInput(text) {
    if (disabled) this.disabled()
    style {
      if (!isValid) border(1.px, Solid, red)
      width(80.px)
    }
    onInput {
      if (it.value.isEmpty()) {
        setValue(min?.takeIf { it > ZERO } ?: ZERO)
        isValid = false
      } else it.value.toBigDecimalOrNull()
        ?.takeIf { it.isBetween(min, max) }
        ?.let {
          setValue(it)
          isValid = true
        }.also { if(it == null) isValid = false }
      text = it.value
    }
  }
}

fun BigDecimal.isBetween(min: BigDecimal? = null, max: BigDecimal? = null) = min?.let{ it <= this } ?: true && max?.let{ it >= this } ?: true

fun String.toBigDecimalOrNull(): BigDecimal? {
  return try {
    toBigDecimal()
  } catch (e: Exception) {
    null
  }
}
