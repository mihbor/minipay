plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.6.10"
  id("org.jetbrains.compose") version composeVersion
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(project(":common"))
        implementation(compose.web.core)
        implementation(compose.runtime)
  
        implementation(npm("qrcode", "1.5.1"))
        implementation(npm("qr-scanner", "1.4.2"))
      }
    }
  }
}
