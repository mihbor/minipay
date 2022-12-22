package ui

import androidx.compose.runtime.*
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.Color.black
import org.jetbrains.compose.web.css.Color.white
import org.jetbrains.compose.web.css.LineStyle.Companion.Solid
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

tailrec fun Element?.itOrAncestorMatches(predicate: (HTMLElement) -> Boolean): Boolean {
  return if (this !is HTMLElement) false
    else if (predicate(this)) true
    else parentElement.itOrAncestorMatches(predicate)
}

@Composable
fun Menu(view: String, setView: (String) -> Unit) {
  var showMenu by remember { mutableStateOf(false) }
  val dismissMenu: (Event) -> Unit = { event ->
    if(!(event.target as? HTMLElement).itOrAncestorMatches { it.id == "menu" }) {
      showMenu = false
    }
  }
  H1({
    style {
      margin(0.px)
    }
  }) {
    Span({
      classes(StyleSheets.clickable)
      onClick {
        showMenu = !showMenu
      }
      style {
        marginRight(5.px)
        padding(2.px)
      }
    }) {
      Text("☰ ")
    }
    Text(view)
  }
  Hr()
  if (showMenu) {
    DisposableEffect("menu") {
      document.addEventListener("click", dismissMenu)
      onDispose {
        document.removeEventListener("click", dismissMenu)
      }
    }
    Div({
      id("menu")
      style {
        padding(10.px)
        backgroundColor(white)
        border(1.px, Solid, black)
        position(Position.Fixed)
        top(40.px)
        left(0.px)
        property("z-index", 1)
      }
    }) {
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Receive")
          showMenu = false
        }
      }) {
        Text("Receive")
      }
      Hr()
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Send")
          showMenu = false
        }
      }) {
        Text("Send")
      }
      Hr()
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Request channel")
          showMenu = false
        }
      }) {
        Text("Request channel")
      }
      Hr()
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Fund channel")
          showMenu = false
        }
      }) {
        Text("Fund channel")
      }
      Hr()
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Channels")
          showMenu = false
        }
      }) {
        Text("List channels")
      }
      Hr()
      Div({
        classes(StyleSheets.clickable)
        onClick {
          setView("Settings")
          showMenu = false
        }
      }) {
        Text("Settings")
      }
    }
  }
}