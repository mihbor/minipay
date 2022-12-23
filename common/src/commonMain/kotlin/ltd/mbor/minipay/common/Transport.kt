package ltd.mbor.minipay.common

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ltd.mbor.minimak.log

const val COLLECTION = "transactions"

suspend fun fetch(id: String): String? = (
  Firebase.firestore.collection(COLLECTION).document(id).get()
    .takeIf { it.exists }?.get("tx")
  )

fun subscribe(id: String, from: Instant? = null): Flow<String> {
  log("subscribing to: $id")
  return Firebase.firestore.collection(COLLECTION).document(id).snapshots.mapNotNull { doc ->
    if(doc.exists) doc else null
  }.filter{
    from == null || from.toEpochMilliseconds() <= (it.get("timestamp") as? Double ?: 0.0)
  }.mapNotNull {
    it.get("tx")
  }
}

suspend fun publish(id: String, content: String) {
  log("publishing to: $id")
  Firebase.firestore.collection(COLLECTION).document(id).set(mapOf("tx" to content, "timestamp" to Clock.System.now().toEpochMilliseconds()))
}
