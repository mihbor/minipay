package com.example.testapp

const val AID = "F222222222"

const val DATA_CHUNK_SIZE = 2100

// ISO-DEP command HEADER for selecting an AID.
// Format: [Class | Instruction | Parameter 1 | Parameter 2]
private const val SELECT_APDU_HEADER = "00A40400"

private const val GET_RESPONSE_HEADER = "00C00000"

val SELECT_OK_SW = "9000".decodeHex()
val UNKNOWN_CMD_SW = "0000".decodeHex()
val SELECT_APDU = buildSelectApdu(AID)
val GET_RESPONSE = buildGetResponseApdu()

/**
 * Build APDU for SELECT AID command. This command indicates which service a reader is
 * interested in communicating with. See ISO 7816-4.
 *
 * @param aid Application ID (AID) to select
 * @return APDU for SELECT AID command
 */
fun buildSelectApdu(aid: String): ByteArray {
  // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
  return (SELECT_APDU_HEADER + String.format("%02X", aid.length / 2) + aid).decodeHex()
}

fun buildGetResponseApdu(): ByteArray {
  return (GET_RESPONSE_HEADER + String.format("%06X", 0)  + String.format("%06X", 65279)).decodeHex()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }

fun String.decodeHex(): ByteArray {
  check(length % 2 == 0) { "Must have an even length" }

  return chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()
}