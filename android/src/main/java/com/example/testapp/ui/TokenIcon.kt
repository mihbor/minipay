package ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.testapp.R
import ltd.mbor.minimak.Balance

@Composable
fun TokenIcon(url: String) {
  AsyncImage(
    model = url,
    contentDescription = url,
    imageLoader = ImageLoader.Builder(LocalContext.current)
      .components {
        add(SvgDecoder.Factory())
      }.build(),
    modifier = Modifier.width(24.dp).height(24.dp),
    placeholder = painterResource(id = R.drawable.ic_tap_and_play)
  )
}

@Composable
fun TokenIcon(tokenId: String, balances: Map<String, Balance>) {
  TokenIcon(balances[tokenId]?.tokenUrl?.takeIf { it.isNotBlank() }
    ?: if (tokenId == "0x00") "minima.svg" else "coins.svg")
}