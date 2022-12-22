package ui

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.ElementBuilder
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement

private open class ElementBuilderImplementation<TElement : Element>(private val tagName: String) : ElementBuilder<TElement> {
  private val el: Element by lazy { document.createElement(tagName) }
  @Suppress("UNCHECKED_CAST")
  override fun create(): TElement = el.cloneNode() as TElement
}

private val Canvas: ElementBuilder<HTMLCanvasElement> = ElementBuilderImplementation("canvas")

@Composable
fun Canvas(
  attrs: AttrBuilderContext<HTMLCanvasElement>? = null,
  content: ContentBuilder<HTMLCanvasElement>? = null
) {
  TagElement(
    elementBuilder = Canvas,
    applyAttrs = attrs,
    content = content
  )
}