package ltd.mbor.minipay

import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*

/**
 * Callback class, invoked when an NFC card is scanned while the device is running in reader mode.
 * Reader mode can be invoked by calling NfcAdapter
 */
class CardReader(dataCallback: ltd.mbor.minipay.CardReader.DataCallback) : ReaderCallback {
  // Weak reference to prevent retain loop. mAccountCallback is responsible for exiting
  // foreground mode before it becomes invalid (e.g. during onPause() or onStop()).
  private val dataCallback: WeakReference<ltd.mbor.minipay.CardReader.DataCallback>

  interface DataCallback {
    fun onDataReceived(data: String)
  }

  /**
   * Callback when a new tag is discovered by the system.
   *
   * @param tag Discovered tag
   */
  override fun onTagDiscovered(tag: Tag) {
    Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "New tag discovered")
    // Android's Host-based Card Emulation (HCE) feature implements the ISO-DEP (ISO 14443-4)
    // protocol.
    val isoDep = IsoDep.get(tag)
    if (isoDep != null) {
      try {
        // Connect to the remote NFC device
        isoDep.connect()
        isoDep.timeout = 15000
        Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "isExtendedLengthApduSupported: ${isoDep.isExtendedLengthApduSupported}, max transceive length is: ${isoDep.maxTransceiveLength}")
        // Build SELECT AID command for our service.
        // This command tells the remote device which service we wish to communicate with.
        Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Requesting remote AID: " + ltd.mbor.minipay.AID)
        val result = isoDep.sendCommand(ltd.mbor.minipay.SELECT_APDU)
        if (ltd.mbor.minipay.SELECT_OK_SW.contentEquals(result.first)) {
          isoDep.communicate()
        }
      } catch (e: IOException) {
        Log.e(ltd.mbor.minipay.CardReader.Companion.TAG, "Error communicating with card: $e")
      }
    }
  }
  fun IsoDep.communicate(partialData: StringBuilder = StringBuilder()) {
    val getResponseResult = sendCommand(ltd.mbor.minipay.GET_RESPONSE)
    if (ltd.mbor.minipay.SELECT_OK_SW.contentEquals(getResponseResult.first)) {
      val data = String(getResponseResult.second, Charset.forName("UTF-8"))
      Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Received: $data")
      partialData.append(data)
      dataCallback.get()!!.onDataReceived(partialData.toString())
      partialData.clear()
    } else if (getResponseResult.first[0] == "61".decodeHex()[0]) {
      val data = String(getResponseResult.second, Charset.forName("UTF-8"))
      Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Received chunk: $data")
      partialData.append(data)
      communicate(partialData)
    }
  }

  private fun IsoDep.sendCommand(command: ByteArray): Pair<ByteArray, ByteArray> {

    Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Sending: " + command.toHex())
    val result = transceive(command)
    val resultLength = result.size
    Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Response length: $resultLength")
    val statusWord = byteArrayOf(result[resultLength - 2], result[resultLength - 1])
    Log.i(ltd.mbor.minipay.CardReader.Companion.TAG, "Status word: " + statusWord.toHex())
    val payload = Arrays.copyOf(result, resultLength - 2)
    return statusWord to payload
  }

  companion object {
    private const val TAG = "CardReader"
  }

  init {
    this.dataCallback = WeakReference(dataCallback)
  }
}