package ltd.mbor.minipay.common

import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.jsonString
import ltd.mbor.minimak.jsonStringOrNull
import ltd.mbor.minipay.common.model.Channel

interface ChannelStorage{
  suspend fun createDB()
  suspend fun getChannel(eltooAddress: String): Channel?
  suspend fun getChannel(myKeys: Channel.Keys): Channel?
  suspend fun getChannels(status: String? = null): List<Channel>
  suspend fun updateChannelStatus(channel: Channel, status: String): Channel
  suspend fun setChannelOpen(multisigAddress: String)
  suspend fun updateChannel(
    channel: Channel,
    triggerTx: String,
    settlementTx: String
  ): Channel

  suspend fun updateChannel(
    channel: Channel,
    channelBalance: Pair<BigDecimal, BigDecimal>,
    sequenceNumber: Int,
    updateTx: String,
    settlementTx: String
  ): Channel

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
    theirAddress: String,
    maximaPK: String? = null
  ): Channel
}

object storage: ChannelStorage {
  override suspend fun createDB() {
    MDS.sql("ALTER TABLE IF EXISTS channel ADD COLUMN IF NOT EXISTS maxima_pk VARCHAR;")
    MDS.sql(//"""DROP TABLE IF EXISTS channel;
      """CREATE TABLE IF NOT EXISTS channel(
        id UUID NOT NULL PRIMARY KEY,
        created_at BIGINT NOT NULL,
        updated_at BIGINT NOT NULL,
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
        eltoo_address VARCHAR,
        maxima_pk VARCHAR
      );""".trimMargin()
    )
  }

  override suspend fun getChannel(eltooAddress: String): Channel? {
    val sql = MDS.sql("SELECT * FROM channel WHERE eltoo_address = '$eltooAddress';")?.jsonObject
    val rows = sql?.get("rows")?.jsonArray ?: emptyList()
    
    return rows.firstOrNull()?.jsonObject?.toChannel()
  }

  override suspend fun getChannel(myKeys: Channel.Keys): Channel? {
    val sql = MDS.sql("SELECT * FROM channel WHERE my_trigger_key = '${myKeys.trigger}' AND my_update_key = '${myKeys.update}' AND my_settle_key = '${myKeys.settle}';")?.jsonObject
    val rows = sql?.get("rows")?.jsonArray ?: emptyList()

    return rows.firstOrNull()?.jsonObject?.toChannel()
  }

  override suspend fun getChannels(status: String?): List<Channel> {
    val sql = MDS.sql("SELECT * FROM channel WHERE status ${status?.let { " = '$it'" } ?: " <> 'DELETED'"} ORDER BY id DESC;")!!
    val rows = sql.jsonObject["rows"]?.jsonArray ?: emptyList()
    
    return rows.map { it.jsonObject }.map(JsonObject::toChannel)
  }

  override suspend fun updateChannelStatus(channel: Channel, status: String): Channel {
    val now = Clock.System.now()
    MDS.sql(
      """UPDATE channel SET
        status = '$status',
        updated_at = ${now.toEpochMilliseconds()}
        WHERE id = '${channel.id}';
      """
    )
    return channel.copy(status = status, updatedAt = now)
  }

  override suspend fun setChannelOpen(multisigAddress: String) {
    val now = Clock.System.now()
    MDS.sql(
      """UPDATE channel SET
        updated_at = ${now.toEpochMilliseconds()},
        status = 'OPEN'
        WHERE multisig_address = '$multisigAddress'
        AND status = 'OFFERED';
      """
    )
  }

  override suspend fun insertChannel(
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
    theirAddress: String,
    maximaPK: String?
  ): Channel {
    val id = uuid4()
    val now = Clock.System.now()
    MDS.sql(
      """INSERT INTO channel(id, 
        status, sequence_number, token_id, my_balance, other_balance,
        my_trigger_key, my_update_key, my_settle_key,
        other_trigger_key, other_update_key, other_settle_key,
        trigger_tx, update_tx, settle_tx, time_lock,
        multisig_address, eltoo_address, my_address, other_address,
        created_at, updated_at, maxima_pk
      ) VALUES ('$id',
        'OFFERED', 0, '$tokenId', ${myBalance.toPlainString()}, ${theirBalance.toPlainString()},
        '${myKeys.trigger}', '${myKeys.update}', '${myKeys.settle}',
        '${theirKeys.trigger}', '${theirKeys.update}', '${theirKeys.settle}',
        '$signedTriggerTx', '', '$signedSettlementTx', $timeLock,
        '$multisigScriptAddress', '$eltooScriptAddress', '$myAddress', '$theirAddress',
        ${now.toEpochMilliseconds()}, ${now.toEpochMilliseconds()}, ${maximaPK?.let { "'$it'" } ?: "NULL" }
      );
    """
    )
    return Channel(
      id = id,
      sequenceNumber = 0,
      status = "OFFERED",
      tokenId = tokenId,
      my = Channel.Side(
        balance = myBalance,
        address = myAddress,
        keys = myKeys
      ),
      their = Channel.Side(
        balance = theirBalance,
        address = theirAddress,
        keys = theirKeys
      ),
      triggerTx = signedTriggerTx,
      settlementTx = signedSettlementTx,
      timeLock = timeLock,
      eltooAddress = eltooScriptAddress,
      multiSigAddress = multisigScriptAddress,
      updatedAt = Clock.System.now(),
      maximaPK = maximaPK
    )
  }

  override suspend fun updateChannel(
    channel: Channel,
    channelBalance: Pair<BigDecimal, BigDecimal>,
    sequenceNumber: Int,
    updateTx: String,
    settlementTx: String
  ): Channel {
    val now = Clock.System.now()
    MDS.sql(
      """UPDATE channel SET
      my_balance = ${channelBalance.first.toPlainString()},
      other_balance = ${channelBalance.second.toPlainString()},
      sequence_number = $sequenceNumber,
      update_tx = '$updateTx',
      settle_tx = '$settlementTx',
      updated_at = ${now.toEpochMilliseconds()}
      WHERE id = '${channel.id}';
    """
    )
    return channel.copy(
      my = channel.my.copy(balance = channelBalance.first),
      their = channel.their.copy(balance = channelBalance.second),
      sequenceNumber = sequenceNumber,
      updateTx = updateTx,
      settlementTx = settlementTx,
      updatedAt = now
    )
  }

  override suspend fun updateChannel(
    channel: Channel,
    triggerTx: String,
    settlementTx: String
  ): Channel {
    val now = Clock.System.now()
    MDS.sql(
      """UPDATE channel SET
        trigger_tx = '$triggerTx',
        settle_tx = '$settlementTx',
        updated_at = ${now.toEpochMilliseconds()}
        WHERE id = '${channel.id}';
      """
    )
    return channel.copy(
      triggerTx = triggerTx,
      settlementTx = settlementTx,
      updatedAt = now
    )
  }
}

private fun JsonObject.toChannel() = Channel(
  id = uuidFrom(jsonString("ID")),
  sequenceNumber = jsonString("SEQUENCE_NUMBER").toInt(),
  status = jsonString("STATUS"),
  tokenId = jsonString("TOKEN_ID"),
  my = Channel.Side(
    balance = jsonString("MY_BALANCE").toBigDecimal(),
    address = jsonString("MY_ADDRESS"),
    keys = Channel.Keys(
      trigger = jsonString("MY_TRIGGER_KEY"),
      update = jsonString("MY_UPDATE_KEY"),
      settle = jsonString("MY_SETTLE_KEY")
    )
  ),
  their = Channel.Side(
    balance = jsonString("OTHER_BALANCE").toBigDecimal(),
    address = jsonString("OTHER_ADDRESS"),
    keys = Channel.Keys(
      trigger = jsonString("OTHER_TRIGGER_KEY"),
      update = jsonString("OTHER_UPDATE_KEY"),
      settle = jsonString("OTHER_SETTLE_KEY")
    )
  ),
  triggerTx = jsonString("TRIGGER_TX"),
  updateTx = jsonString("UPDATE_TX"),
  settlementTx = jsonString("SETTLE_TX"),
  timeLock = jsonString("TIME_LOCK").toInt(),
  eltooAddress = jsonString("ELTOO_ADDRESS"),
  multiSigAddress = jsonString("MULTISIG_ADDRESS"),
  maximaPK = jsonStringOrNull("MAXIMA_PK"),
  updatedAt = Instant.fromEpochMilliseconds(jsonString("UPDATED_AT").toLong())
)

