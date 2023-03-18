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

  @Composable
  fun MenuItem(view: String, label: String = view) {
    Div({
      classes(StyleSheets.clickable)
      onClick {
        setView(view)
        showMenu = false
      }
    }) {
      Text(label)
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
      MenuItem("Receive")
      Hr()
      MenuItem("Send")
      Hr()
      MenuItem("Create Channel")
      Hr()
      MenuItem("Channels", "Channel Listing")
      Hr()
      MenuItem("Channel Events")
      Hr()
      MenuItem("Contacts")
      Hr()
      MenuItem("Settings")
    }
  }
}