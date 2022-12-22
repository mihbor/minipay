@file:JsModule("qr-scanner")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals

import org.w3c.dom.*
import org.w3c.dom.svg.SVGImageElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.File
import tsstdlib.OffscreenCanvas
import kotlin.js.Promise

@JsName("default")
external open class QrScanner {
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, onDecodeError: (error: String) -> Unit = definedExternally, calculateScanRegion: (video: HTMLVideoElement) -> ScanRegion = definedExternally, preferredCamera: String = definedExternally)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, onDecodeError: (error: String) -> Unit = definedExternally)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, onDecodeError: (error: String) -> Unit = definedExternally, calculateScanRegion: (video: HTMLVideoElement) -> ScanRegion = definedExternally)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, onDecodeError: (error: String) -> Unit = definedExternally, canvasSize: Number = definedExternally, preferredCamera: String = definedExternally)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, onDecodeError: (error: String) -> Unit = definedExternally, canvasSize: Number = definedExternally)
    constructor(video: HTMLVideoElement, onDecode: (result: String) -> Unit, canvasSize: Number = definedExternally)
    open fun hasFlash(): Promise<Boolean>
    open fun isFlashOn(): Boolean
    open fun toggleFlash(): Promise<Unit>
    open fun turnFlashOff(): Promise<Unit>
    open fun turnFlashOn(): Promise<Unit>
    open fun destroy()
    open fun start(): Promise<Unit>
    open fun stop()
    open fun pause(stopStreamImmediately: Boolean = definedExternally): Promise<Boolean>
    open fun setCamera(facingModeOrDeviceId: String /* "environment" | "user" */): Promise<Unit>
    open fun setGrayscaleWeights(red: Number, green: Number, blue: Number, useIntegerApproximation: Boolean = definedExternally)
    open fun setInversionMode(inversionMode: String /* "original" | "invert" | "both" */)
    interface ScanRegion {
        var x: Number?
            get() = definedExternally
            set(value) = definedExternally
        var y: Number?
            get() = definedExternally
            set(value) = definedExternally
        var width: Number?
            get() = definedExternally
            set(value) = definedExternally
        var height: Number?
            get() = definedExternally
            set(value) = definedExternally
        var downScaledWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var downScaledHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface Camera {
        var id: DeviceId
        var label: String
    }

    companion object {
        var DEFAULT_CANVAS_SIZE: Number
        var NO_QR_CODE_FOUND: String
        var WORKER_PATH: String
        fun hasCamera(): Promise<Boolean>
        fun listCameras(requestLabels: Boolean): Promise<Array<Camera>>
        fun scanImage(imageOrFileOrUrl: HTMLCanvasElement, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: HTMLVideoElement, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: ImageBitmap, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: HTMLImageElement, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: File, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: URL, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun scanImage(imageOrFileOrUrl: String, scanRegion: ScanRegion? = definedExternally, worker: Worker? = definedExternally, canvas: HTMLCanvasElement? = definedExternally, disallowCanvasResizing: Boolean = definedExternally, alsoTryWithoutScanRegion: Boolean = definedExternally): Promise<String>
        fun createQrEngine(workerPath: String = definedExternally): Promise<dynamic /* Worker | BarcodeDetector */>
    }
}

external interface `T$0` {
    var formats: Array<String>
}

external interface `T$1` {
    var rawValue: String
}

external open class BarcodeDetector(options: `T$0` = definedExternally) {
    open fun detect(image: HTMLImageElement): Promise<Array<`T$1`>>
    open fun detect(image: SVGImageElement): Promise<Array<`T$1`>>
    open fun detect(image: HTMLVideoElement): Promise<Array<`T$1`>>
    open fun detect(image: HTMLCanvasElement): Promise<Array<`T$1`>>
    open fun detect(image: ImageBitmap): Promise<Array<`T$1`>>
    open fun detect(image: OffscreenCanvas): Promise<Array<`T$1`>>
    open fun detect(image: Blob): Promise<Array<`T$1`>>
    open fun detect(image: ImageData): Promise<Array<`T$1`>>

    companion object {
        fun getSupportedFormats(): Promise<Array<String>>
    }
}