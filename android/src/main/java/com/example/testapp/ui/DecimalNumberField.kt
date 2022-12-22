package com.example.testapp.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

@Composable
fun DecimalNumberField(
  value: BigDecimal?,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = LocalTextStyle.current,
  min: BigDecimal? = null,
  max: BigDecimal? = null,
  enabled: Boolean = true,
  setValue: (BigDecimal?) -> Unit = {}
) {
  var text by remember { mutableStateOf(value?.toPlainString() ?: "") }
  var isValid by remember { mutableStateOf(value?.isBetween(min, max) ?: false) }
  value.takeUnless { it == text.toBigDecimalOrNull() || it == BigDecimal.ZERO && text.isEmpty()}
    ?.let {
      text = it.toPlainString()
      isValid = true
    }
  OutlinedTextField(
    value = text,
    textStyle = textStyle,
    onValueChange = {
      if (it.isEmpty()) {
        setValue(min?.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ZERO)
        isValid = false
      } else it.toBigDecimalOrNull()
        ?.takeIf { it.isBetween(min, max) }
        ?.let {
          setValue(it)
          isValid = true
        }.also { if(it == null) isValid = false }
      text = it
    },
    enabled = enabled,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    modifier = modifier
  )
}

fun String.toBigDecimalOrNull(): BigDecimal? {
  return try {
    toBigDecimal()
  } catch (e: Exception) {
    null
  }
}

fun BigDecimal.isBetween(min: BigDecimal? = null, max: BigDecimal? = null) = min?.let{ it <= this } ?: true && max?.let{ it >= this } ?: true
