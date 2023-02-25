package ltd.mbor.minipay.logic

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import ltd.mbor.minipay.common.transport.FirebaseTransport

var firebaseTransport: FirebaseTransport? = null

fun initFirebase(context: Context): FirebaseTransport {
  return firebaseTransport ?: FirebaseTransport(
    Firebase.initialize(
      context = context,
      options = FirebaseOptions(
        apiKey= "AIzaSyAxCuQGZTOrHLS-qdaUN2LdEkwHSy3CDpw",
        authDomain= "mini-payments.firebaseapp.com",
        projectId= "mini-payments",
        storageBucket= "mini-payments.appspot.com",
        gcmSenderId= "845857085139",
        applicationId= "1:845857085139:web:17b6f44725a166fee7a626"
      )
    )
  ).also { firebaseTransport = it }
}
