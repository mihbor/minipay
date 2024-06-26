plugins {
  id("org.jetbrains.compose")
  id("com.android.application")
  kotlin("android")
}

dependencies {
  implementation(project(":common"))
  implementation("androidx.core:core-ktx:1.9.0") // 1.10.0 breaks compose previews
  implementation("androidx.compose.material:material:$composeVersion")
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
  implementation("androidx.activity:activity-compose:1.7.0")
  implementation("androidx.datastore:datastore-preferences:1.0.0")

  implementation("com.google.zxing:core:3.5.1")
  implementation("com.journeyapps:zxing-android-embedded:4.3.0")
  implementation("com.google.firebase:firebase-common:20.3.2")
  implementation("com.google.firebase:firebase-firestore:24.5.0")
  implementation("io.coil-kt:coil-compose:$coilVersion")
  implementation("io.coil-kt:coil-svg:$coilVersion")
}
base {
  archivesName.set("${rootProject.name}-$version")
}
android {
  compileSdk = androidCompileSdk
  namespace = "$group.${rootProject.name}"
  
  defaultConfig {
    applicationId = "$group.${rootProject.name}"
    minSdk = androidMinSdk
    targetSdk = androidCompileSdk
    versionCode = 1
    versionName = "$version"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.3.2"
  }
}
