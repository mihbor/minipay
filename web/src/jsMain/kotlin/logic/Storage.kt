package logic

import Channel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.jsonString
import kotlin.js.Date

suspend fun createDB() {
  MDS.sql(//"""DROP TABLE IF EXISTS channel;
    """CREATE TABLE IF NOT EXISTS channel(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR,
    token_id VARCHAR,
    my_balance DECIMAL(20,10),
    other_balance DECIMAL(20,10),
    my_address VARCHAR,
    other_address VARCHAR,
    my_trigger_key VARCHAR,
    my_update_key VARCHAR,
    my_settle_key VARCHAR,
    other_trigger_key VARCHAR,
    other_update_key VARCHAR,
    other_settle_key VARCHAR,
    sequence_number INT,
    time_lock INT,
    trigger_tx VARCHAR,
    update_tx VARCHAR,
    settle_tx VARCHAR,
    multisig_address VARCHAR,
    eltoo_address VARCHAR
  );""".trimMargin())
}

suspend fun getChannels(status: String? = null): List<Channel> {
  val sql = MDS.sql("SELECT * FROM channel${status?.let { " WHERE status = '$it'" } ?: ""} ORDER BY id DESC;")!!
  val rows = sql.jsonObject["rows"]!!.jsonArray
  
  return rows.map { row ->
    Channel(
      id = row.jsonString("ID")!!.toInt(),
      sequenceNumber = row.jsonString("SEQUENCE_NUMBER")!!.toInt(),
      status = row.jsonString("STATUS")!!,
      tokenId = row.jsonString("TOKEN_ID")!!,
      my = Channel.Side(
        balance = row.jsonString("MY_BALANCE")!!.toBigDecimal(),
        address = row.jsonString("MY_ADDRESS")!!,
        keys = Channel.Keys(
          trigger = row.jsonString("MY_TRIGGER_KEY")!!,
          update = row.jsonString("MY_UPDATE_KEY")!!,
          settle = row.jsonString("MY_SETTLE_KEY")!!
        )
      ),
      their = Channel.Side(
        balance = row.jsonString("OTHER_BALANCE")!!.toBigDecimal(),
        address = row.jsonString("OTHER_ADDRESS")!!,
        keys = Channel.Keys(
          trigger = row.jsonString("OTHER_TRIGGER_KEY")!!,
          update = row.jsonString("OTHER_UPDATE_KEY")!!,
          settle = row.jsonString("OTHER_SETTLE_KEY")!!
        )
      ),
      triggerTx = row.jsonString("TRIGGER_TX")!!,
      updateTx = row.jsonString("UPDATE_TX")!!,
      settlementTx = row.jsonString("SETTLE_TX")!!,
      timeLock = row.jsonString("TIME_LOCK")!!.toInt(),
      eltooAddress = row.jsonString("ELTOO_ADDRESS")!!,
      updatedAt = Date.parse(row.jsonString("UPDATED_AT")!!).toLong()
    )
  }
}

suspend fun updateChannelStatus(channel: Channel, status: String): Channel {
  MDS.sql("""UPDATE channel SET
    status = '$status',
    updated_at = NOW()
    WHERE id = ${channel.id};
  """)
  return channel.copy(status = status, updatedAt = Date.now().toLong())
}

suspend fun setChannelOpen(multisigAddress: String) {
  MDS.sql("""UPDATE channel SET
    updated_at = NOW(),
    status = 'OPEN'
    WHERE multisig_address = '$multisigAddress'
    AND status = 'OFFERED';
  """)
}

suspend fun insertChannel(
  tokenId: String,
  myBalance: BigDecimal,
  theirBalance: BigDecimal,
  myKeys: Channel.Keys,
  theirKeys: Channel.Keys,
  signedTriggerTx: String,
  signedSettlementTx: String,
  timeLock: Int,
  multisigScriptAddress: String,
  eltooScriptAddress: String,
  myAddress: String,
  otherAddress: String
): Int {
  
  MDS.sql("""INSERT INTO channel(
      status, sequence_number, token_id, my_balance, other_balance,
      my_trigger_key, my_update_key, my_settle_key,
      other_trigger_key, other_update_key, other_settle_key,
      trigger_tx, update_tx, settle_tx, time_lock,
      multisig_address, eltoo_address, my_address, other_address
    ) VALUES (
      'OFFERED', 0, '$tokenId', ${myBalance.toPlainString()}, ${theirBalance.toPlainString()},
      '${myKeys.trigger}', '${myKeys.update}', '${myKeys.settle}',
      '${theirKeys.trigger}', '${theirKeys.update}', '${theirKeys.settle}',
      '$signedTriggerTx', '', '$signedSettlementTx', $timeLock,
      '$multisigScriptAddress', '$eltooScriptAddress', '$myAddress', '$otherAddress'
    );
  """)
  val sql = MDS.sql("SELECT IDENTITY() as ID;")
  val results = sql!!.jsonObject["rows"]!!.jsonArray
  return  results[0].jsonString("ID")!!.toInt()
}

suspend fun updateChannel(
  channel: Channel,
  channelBalance: Pair<BigDecimal, BigDecimal>,
  sequenceNumber: Int,
  updateTx: String,
  settlementTx: String
): Channel {
  MDS.sql("""UPDATE channel SET
    my_balance = ${channelBalance.first.toPlainString()},
    other_balance = ${channelBalance.second.toPlainString()},
    sequence_number = $sequenceNumber,
    update_tx = '$updateTx',
    settle_tx = '$settlementTx',
    updated_at = NOW()
    WHERE id = ${channel.id};
  """)
  return channel.copy(
    my = channel.my.copy(balance = channelBalance.first),
    their = channel.their.copy(balance = channelBalance.second),
    sequenceNumber = sequenceNumber,
    updateTx = updateTx,
    settlementTx = settlementTx,
    updatedAt = Date.now().toLong()
  )
}