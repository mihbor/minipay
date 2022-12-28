plugins {
  id("org.jetbrains.compose")
  id("com.android.application")
  kotlin("android")
}

dependencies {
  implementation(project(":common"))
  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.compose.material:material:$composeVersion")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
  implementation("androidx.activity:activity-compose:1.6.0")
  implementation("com.google.zxing:core:3.5.1")
  implementation("com.journeyapps:zxing-android-embedded:4.3.0")
  implementation("com.google.firebase:firebase-common:20.1.0")
  implementation("com.google.firebase:firebase-firestore:24.1.0")
  implementation("io.coil-kt:coil-compose:$coilVersion")
  implementation("io.coil-kt:coil-svg:$coilVersion")
}

android {
  compileSdk = 33
  namespace = "com.example.testapp"
  
  defaultConfig {
    applicationId = "ltd.mbor.minipay"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "0.2-SNAPSHOT"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.3.2"
  }
}