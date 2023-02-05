plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
  id("com.android.library")
}

kotlin {
  android()
  js(IR) {
    browser()
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

        api("com.ionspin.kotlin:bignum:$bignumVersion")
        api("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")
        
        api("dev.gitlive:firebase-firestore:1.6.2")

        api("ltd.mbor:minimak:0.3.1")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
      }
    }
    val androidMain by getting
    val jsMain by getting

    androidMain.dependsOn(commonMain)
    jsMain.dependsOn(commonMain)
  }
}

android {
  namespace = "ltd.mbor.minipay.common"
  compileSdk = androidCompileSdk
  defaultConfig {
    minSdk = androidMinSdk
    targetSdk = androidCompileSdk
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(1, "hours")
}
