package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.RangeInput

@Composable
fun Switch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?
) {
  RangeInput(
    value = if (checked) 1 else 0,
    min = 0,
    max = 1,
    step = 1
  ) {
    onClick {
      onCheckedChange?.let { onCheckedChange(!checked) }
    }
    style {
      width(35.px)
      property("vertical-align", "middle")
    }
  }
}