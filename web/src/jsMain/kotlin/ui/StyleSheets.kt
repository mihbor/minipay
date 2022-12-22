package ui

import org.jetbrains.compose.web.css.*

object StyleSheets : StyleSheet() {
  val container by style {
    border(1.px, LineStyle.Solid, Color.black)
    borderRadius(10.px)
    padding(10.px)
  }
  val clickable by style {
    hover(self) style {
      cursor("pointer")
    }
  }
}