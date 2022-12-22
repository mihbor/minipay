package logic

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import ltd.mbor.minimak.log
import kotlin.js.Date

val firebaseApp: FirebaseApp = Firebase.initialize(
  options = FirebaseOptions(
    apiKey= "AIzaSyAxCuQGZTOrHLS-qdaUN2LdEkwHSy3CDpw",
    authDomain= "mini-payments.firebaseapp.com",
    projectId= "mini-payments",
    storageBucket= "mini-payments.appspot.com",
    gcmSenderId= "845857085139",
    applicationId= "1:845857085139:web:17b6f44725a166fee7a626"
  )
)

const val COLLECTION = "transactions"

suspend fun fetch(id: String): String? = (
  Firebase.firestore.collection(COLLECTION).document(id).get()
    .takeIf { it.exists }?.get("tx")
  )

fun subscribe(id: String, from: Long? = null): Flow<String> {
  firebaseApp
  log("subscribing to: $id")
  return Firebase.firestore.collection(COLLECTION).document(id).snapshots.mapNotNull { doc ->
    if(doc.exists) doc else null
  }.filter{
    from == null || from <= (it.get("timestamp") as? Double ?: 0.0)
  }.mapNotNull {
    it.get("tx")
  }
}

suspend fun publish(id: String, content: String) {
  firebaseApp
  log("publishing to: $id")
  Firebase.firestore.collection(COLLECTION).document(id).set(mapOf("tx" to content, "timestamp" to Date.now()))
}
