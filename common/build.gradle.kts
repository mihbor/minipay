plugins {
  kotlin("multiplatform")
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
  
        api("com.ionspin.kotlin:bignum:$bignumVersion")
        api("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")
  
        api("ltd.mbor:minimak:0.2-SNAPSHOT")
      }
    }
    val androidMain by getting
    val jsMain by getting

    androidMain.dependsOn(commonMain)
    jsMain.dependsOn(commonMain)
  }
}

android {
  compileSdk = 33
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  defaultConfig {
    minSdk = 24
    targetSdk = 33
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}