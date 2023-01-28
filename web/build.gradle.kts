plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
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

tasks.register<Copy>("updateDappVersion") {
  from("src/jsMain/resources/dapp.conf")
  into(layout.buildDirectory.dir("processedResources/js/main/"))
  filter { line -> line.replace("\"version\": \".*\"".toRegex(), "\"version\": \"$version\"") }
}

tasks["jsProductionExecutableCompileSync"].dependsOn("updateDappVersion")
tasks["jsBrowserProductionExecutableDistributeResources"].dependsOn("updateDappVersion")

tasks.register<Zip>("minidappDistribution") {
  dependsOn("jsBrowserDistribution", "updateDappVersion")
  archiveFileName.set("${rootProject.name}.mds.zip")
  destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
  from(layout.buildDirectory.dir("distributions"))
  exclude("*.LICENSE.txt")
}