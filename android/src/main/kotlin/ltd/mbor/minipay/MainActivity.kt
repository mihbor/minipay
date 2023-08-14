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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getAddress
import ltd.mbor.minimak.importTx
import ltd.mbor.minimak.log
import ltd.mbor.minipay.common.ChannelService
import ltd.mbor.minipay.common.model.ChannelInvite
import ltd.mbor.minipay.common.model.PaymentRequestReceived
import ltd.mbor.minipay.common.model.Prefs
import ltd.mbor.minipay.common.model.Transport.NFC
import ltd.mbor.minipay.common.newTxId
import ltd.mbor.minipay.common.scope
import ltd.mbor.minipay.common.storage
import ltd.mbor.minipay.common.storage.getChannel
import ltd.mbor.minipay.logic.*
import ltd.mbor.minipay.ui.MainView
import ltd.mbor.minipay.ui.theme.MiniPayTheme
import ltd.mbor.minipay.ui.toBigDecimalOrNull

val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

const val TAG = "MainActivity"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val UID_KEY = stringPreferencesKey("uid")
val HOST_KEY = stringPreferencesKey("host")
val PORT_KEY = intPreferencesKey("port")

var view by mutableStateOf("MiniPay")
var channelInvite by mutableStateOf(ChannelInvite.EMPTY)

class MainActivity : ComponentActivity(), CardReader.DataCallback {
  var isReaderModeOn by mutableStateOf(true)

  var prefs by mutableStateOf(Prefs("", "localhost", 9004))
  var address by mutableStateOf("")
  var tokenId by mutableStateOf("0x00")
  var amount by mutableStateOf(ZERO)

  var cardReader: CardReader = CardReader(this)

  fun init(prefs: Prefs) {
    this.prefs = prefs
    scope.launch {
      initMDS(prefs.uid, prefs.host, prefs.port, applicationContext)
      if (inited) applicationContext.dataStore.edit {
        it[UID_KEY] = prefs.uid
        it[HOST_KEY] = prefs.host
        it[PORT_KEY] = prefs.port
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    channelService = ChannelService(MDS, storage, initFirebase(applicationContext), channels, events)

    intent?.data?.let{ uri ->
      Log.i(TAG, uri.toString())
      val action = uri.path
      if (action == "/emit") {
        scope.launch { emitReceive(address) }
      } else enableReaderMode()

      uri.getQueryParameter("uid")?.let { init(Prefs(it, checkNotNull(uri.host), uri.port)) }
      uri.getQueryParameter("address")?.let { address = it }
      uri.getQueryParameter("token")?.let { tokenId = it }
      uri.getQueryParameter("amount")?.toBigDecimalOrNull()?.let{ amount = it }
    } ?: enableReaderMode()
    view = when(intent?.data?.path) {
      "/send" -> "Send"
      "/emit" -> "Receive"
      else -> view
    }
    if (prefs.uid.isBlank()) scope.launch{
      val uidFlow = applicationContext.dataStore.data.map{
        Prefs(it[UID_KEY] ?: "", it[HOST_KEY] ?: "localhost", it[PORT_KEY] ?: 9004)
      }
      uidFlow.first().let { init(it) }
    }
    setContent {
      MiniPayTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          MainView(
            inited = inited,
            prefs = prefs,
            setPrefs = this::init,
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
            setView = { view = it },
            channelInvite = channelInvite,
            setChannelInvite = { channelInvite = it }
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
      try {
        this@MainActivity.address = address ?: MDS.getAddress().address
      } catch (e: NullPointerException) {
        Toast.makeText(applicationContext, "Error getting status. Wrong UID?", Toast.LENGTH_LONG).show()
        log(e.toString())
        return@launch
      }
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
        events.add(PaymentRequestReceived(channel, updateTxId, settleTxId,sequenceNumber, channelBalance, transport = NFC))
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
