package com.example.testapp

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * This service will be invoked for any terminals selecting AID of 0xF22222222.
 * See src/main/res/xml/aid_list.xml for more details.
 *
 * Note: This is a low-level interface, card emulation only provides a
 * byte-array based communication channel.
 */
class CardService : HostApduService() {
  private var data: String? = ""
  private var dataBytes: ByteArray = ByteArray(0)
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Log.i(TAG, "Received start command")
    Log.i(TAG, "SELECT_APDU is: " + SELECT_APDU.toHex())
    Log.i(TAG, "READ_BINARY is: " + GET_RESPONSE.toHex())
    intent.extras?.let {
      data = it.getString("data")
      dataBytes = data?.toByteArray() ?: ByteArray(0)
      Log.i(TAG, "Data of size " + data?.length + " : " + data)
    }
    return START_NOT_STICKY
  }

  /**
   * Called if the connection to the NFC card is lost, in order to let the application know the
   * cause for the disconnection (either a lost link, or another AID being selected by the
   * reader).
   *
   * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
   */
  override fun onDeactivated(reason: Int) {
    Log.w(TAG, "disconnected, reason: $reason")
  }

  /**
   * This method will be called when a command APDU has been received from a remote device. A
   * response APDU can be provided directly by returning a byte-array in this method. In general
   * response APDUs must be sent as quickly as possible, given the fact that the user is likely
   * holding his device over an NFC reader when this method is called.
   * This method is running on the main thread of your application. If you
   * cannot return a response APDU immediately, return null and use the [ ][.sendResponseApdu] method later.
   *
   * @param commandApdu The APDU that received from the remote device
   * @param extras A bundle containing extra data. May be null.
   * @return a byte-array containing the response APDU, or null if no response APDU can be sent
   * at this point.
   */
  override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
    Log.w(TAG, "Received APDU: " + commandApdu.toHex())
    // If the APDU matches the SELECT AID command for this service,
    // send the SELECT_OK status trailer (0x9000).
    return if (SELECT_APDU.contentEquals(commandApdu)) {
//            String account = AccountStorage.GetAccount(this);
      Log.w(TAG, "Sending OK")
      SELECT_OK_SW
    } else if (GET_RESPONSE.contentEquals(commandApdu)) {
      val currentBytes = dataBytes.take(DATA_CHUNK_SIZE).toByteArray()
      Log.w(TAG, "Sending data of size " + currentBytes.size + " data: " + String(currentBytes))
      currentBytes + if (dataBytes.size <= DATA_CHUNK_SIZE)
        SELECT_OK_SW
      else "61FF".decodeHex().also {
        dataBytes = dataBytes.toList().subList(DATA_CHUNK_SIZE, dataBytes.size).toByteArray()
      }
    } else UNKNOWN_CMD_SW
  }

  companion object {
    private const val TAG = "CardService"
  }
}