package ltd.mbor.minipay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import ltd.mbor.minimak.Balance
import ltd.mbor.minipay.R

@Composable
fun TokenIcon(url: String, modifier: Modifier = Modifier, size: Int = 24) {
  AsyncImage(
    model = url,
    contentDescription = url,
    imageLoader = ImageLoader.Builder(LocalContext.current)
      .components {
        add(SvgDecoder.Factory())
      }.build(),
    modifier = modifier.requiredSize(size.dp),
    placeholder = painterResource(id = R.drawable.ic_tap_and_play)
  )
}

@Composable
fun TokenIcon(painter: Painter, modifier: Modifier = Modifier, size: Int = 24) {
  Image(
    painter,
    "token logo",
    modifier = modifier.requiredSize(size.dp),
  )
}

@Composable
fun TokenIcon(tokenId: String, balances: Map<String, Balance>, modifier: Modifier = Modifier, size: Int = 24) {
  balances[tokenId]?.tokenUrl?.takeIf { it.isNotBlank() }?.let{ TokenIcon(it, modifier, size) }
  ?: TokenIcon(painterResource(if (tokenId == "0x00") R.drawable.minima else R.drawable.coins), modifier, size)
}

@Composable @Preview
fun PreviewMinimaTokenIcon() {
  TokenIcon("0x00", emptyMap())
}

@Composable @Preview
fun PreviewUnknownTokenIcon() {
  TokenIcon("0x0123", emptyMap())
}