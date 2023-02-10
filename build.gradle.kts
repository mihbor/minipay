import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform") version kotlinVersion apply false
  kotlin("plugin.serialization") version kotlinVersion apply true
  kotlin("android") version kotlinVersion apply false
  id("com.android.application") version agpVersion apply false
  id("com.android.library") version agpVersion apply false
  id("org.jetbrains.compose") version composeVersion apply false
  id("org.jetbrains.kotlinx.kover") version "0.6.1" apply true
}

allprojects {
  group = "ltd.mbor"
  version = "0.2.7"

  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.github.com/mihbor/MinimaK") {
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
  apply(plugin = "org.jetbrains.kotlinx.kover")
  koverMerged {
    enable()
  }
  
  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "11"
    }
  }
}

tasks.register<Copy>("copyApk") {
  val androidPackage = project(":android").tasks["packageDebug"]
  val webDistribution = project(":web").tasks["jsBrowserDistribution"]
  dependsOn(androidPackage, webDistribution)
  from(androidPackage)
  include("**.apk")
  into(project(":web").layout.buildDirectory.dir("processedResources/js/main/"))
  rename{ "minipay.apk" }
}

tasks.register("minidappWithApk") {
  dependsOn("copyApk", ":web:minidappDistribution")
}

