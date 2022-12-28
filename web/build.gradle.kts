plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.6.10"
  id("org.jetbrains.compose") version composeVersion
}
base {
  archivesName.set("minipay")
}
kotlin {
  js(IR) {
    browser{
      webpackTask {
        sourceMaps = true
      }
    }
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

tasks.register<Zip>("minidappDistribution") {
  dependsOn("jsBrowserDistribution")
  archiveFileName.set("${rootProject.name}.mds.zip")
  destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
  from(layout.buildDirectory.dir("distributions"))
  exclude("*.LICENSE.txt")
}
