@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
package tsstdlib

import org.khronos.webgl.WebGLContextAttributes
import org.w3c.dom.*
import org.w3c.dom.events.EventTarget
import org.w3c.files.Blob
import kotlin.js.Promise

external interface ImageEncodeOptions {
    var quality: Number?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OffscreenCanvas : EventTarget {
    var height: Number
    var width: Number
    fun convertToBlob(options: ImageEncodeOptions = definedExternally): Promise<Blob>
    fun getContext(contextId: String /* "2d" */, options: CanvasRenderingContext2DSettings = definedExternally): OffscreenCanvasRenderingContext2D?
    fun getContext(contextId: String /* "2d" | "bitmaprenderer" | "webgl" | "webgl2" */): dynamic /* WebGL2RenderingContext | OffscreenCanvasRenderingContext2D? | ImageBitmapRenderingContext? | WebGLRenderingContext? | WebGL2RenderingContext? */
    fun getContext(contextId: String /* "bitmaprenderer" */, options: ImageBitmapRenderingContextSettings = definedExternally): ImageBitmapRenderingContext?
    fun getContext(contextId: String /* "webgl" | "webgl2" */, options: WebGLContextAttributes = definedExternally): dynamic /* WebGLRenderingContext | WebGL2RenderingContext */
    fun getContext(contextId: String /* "2d" | "bitmaprenderer" | "webgl" | "webgl2" */, options: Any = definedExternally): dynamic /* OffscreenCanvasRenderingContext2D? | ImageBitmapRenderingContext? | WebGLRenderingContext? | WebGL2RenderingContext? */
    fun transferToImageBitmap(): ImageBitmap
}

external interface OffscreenCanvasRenderingContext2D : CanvasCompositing, CanvasDrawImage, CanvasDrawPath, CanvasFillStrokeStyles, CanvasFilters, CanvasImageData, CanvasImageSmoothing, CanvasPath, CanvasPathDrawingStyles, CanvasRect, CanvasShadowStyles, CanvasState, CanvasText, CanvasTextDrawingStyles, CanvasTransform {
    var canvas: OffscreenCanvas
    fun commit()
}
