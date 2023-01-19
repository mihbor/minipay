package ltd.mbor.minipay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getAddress
import ltd.mbor.minimak.importTx
import ltd.mbor.minipay.common.getChannel
import ltd.mbor.minipay.common.newTxId
import ltd.mbor.minipay.logic.PaymentRequestReceived
import ltd.mbor.minipay.logic.channelUpdateAck
import ltd.mbor.minipay.logic.events
import ltd.mbor.minipay.logic.initFirebase
import ltd.mbor.minipay.ui.MainView
import ltd.mbor.minipay.ui.theme.MiniPayTheme
import ltd.mbor.minipay.ui.toBigDecimalOrNull

val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

const val TAG = "MainActivity"

val scope = MainScope()

var view by mutableStateOf("Settings")

class MainActivity : ComponentActivity(), CardReader.DataCallback {
  var isReaderModeOn by mutableStateOf(true)

  var uid by mutableStateOf("")
  var address by mutableStateOf("")
  var tokenId by mutableStateOf("0x00")
  var amount by mutableStateOf(ZERO)

  var cardReader: CardReader = CardReader(this)

  fun init(uid: String, host: String = "localhost", port: Int = 9004) {
    this.uid = uid
    scope.launch {
      initMDS(uid, host, port)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initFirebase(applicationContext)

    intent?.data?.let{ uri ->
      Log.i(TAG, uri.toString())
      val action = uri.path
      if (action == "/emit") {
        scope.launch { emitReceive(address) }
      } else enableReaderMode()

      uri.getQueryParameter("uid")?.let { init(it, uri.host ?: "localhost", uri.port) }
      uri.getQueryParameter("address")?.let { address = it }
      uri.getQueryParameter("token")?.let { tokenId = it }
      uri.getQueryParameter("amount")?.toBigDecimalOrNull()?.let{ amount = it }
    } ?: enableReaderMode()
    view = when(intent?.data?.path) {
      "/send" -> "Send"
      "/emit" -> "Receive"
      else -> "Settings"
    }
    setContent {
      MiniPayTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          MainView(
            inited = inited,
            uid = uid,
            setUid = this::init,
            balances = balances,
            tokens = tokens,
            address = address,
            setAddress = { address = it},
            amount = amount,
            setAmount = ::updateAmount,
            tokenId = tokenId,
            setTokenId = { tokenId = it },
            startEmitting = ::emitReceive,
            stopEmitting = ::enableReaderMode,
            activity = this,
            view = view,
            setView = { view = it }
          )
        }
      }
    }

    val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    if (nfcAdapter == null) {
      Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
      System.err.println("NFC is not available")
    }
  }

  fun enableReaderMode() {
    Log.i(TAG, "Enabling reader mode")
    Toast.makeText(this, "Enabling reader mode", Toast.LENGTH_LONG).show()
    address = ""
    amount = ZERO

    val activity: Activity = this
    val nfc = NfcAdapter.getDefaultAdapter(activity)
    nfc?.enableReaderMode(
      activity,
      cardReader,
      READER_FLAGS,
      null
    )
    isReaderModeOn = true
  }

  private fun emitReceive(address: String? = null) {
    disableReaderMode()
    scope.launch {
      this@MainActivity.address = address ?: MDS.getAddress().address
      applicationContext.sendDataToService("$address;$tokenId;${amount.toPlainString()}")
    }
  }

  fun disableReaderMode() {
    Log.i(TAG, "Disabling reader mode")
    Toast.makeText(this, "Disabling reader mode", Toast.LENGTH_LONG).show()
    isReaderModeOn = false
    NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
  }

  private fun updateAmount(amount: BigDecimal?) {
    Log.i(TAG, "Update amount")
    this.amount = amount ?: ZERO
    if (!isReaderModeOn) applicationContext.sendDataToService("$address;$tokenId;${this.amount.toPlainString()}")
  }

  override fun onDataReceived(data: String) {
    Log.i(TAG, "data received length: ${data.length}, " + data)
    val splits = data.split(";")
    if (splits[0] == "TXN_REQUEST") {
      val (_, updateTxText, settleTxText) = splits
      Log.i(TAG, "TXN_REQUEST received, updateTxLength: ${updateTxText.length}, settleTxLength: ${settleTxText.length}")
      scope.launch {
        val updateTxId = newTxId()
        val updateTx = MDS.importTx(updateTxId, updateTxText)
        val settleTxId = newTxId()
        val settleTx = MDS.importTx(settleTxId, settleTxText)
        val channel = getChannel(updateTx.outputs.first().address)!!
        val sequenceNumber = settleTx.state.first{ it.port == 99 }.data.toInt()
        check(sequenceNumber == channel.sequenceNumber + 1)
        val channelBalance = settleTx.outputs.first{ it.address == channel.my.address }.tokenAmount to settleTx.outputs.first{ it.address == channel.their.address }.tokenAmount
        events.add(PaymentRequestReceived(channel, updateTxId, settleTxId,sequenceNumber, channelBalance, isNfc = true))
        view = "events"
      }
    } else if (splits[0] == "TXN_UPDATE_ACK") {
      val (_, updateTxText, settleTxText) = splits
      scope.launch {
        channelUpdateAck(updateTxText, settleTxText)
      }
    } else {
      this.address = splits[0]
      if (splits.size > 1) this.tokenId = splits[1]
      if (splits.size > 2) this.amount = splits[2].toBigDecimal()
    }
  }
}

fun Context.sendDataToService(data: String) {
  val intent = Intent(this, CardService::class.java)
  intent.putExtra("data", data)
  this.startService(intent)
}
