allprojects {
  group = "ltd.mbor"
  version = "0.2.2"

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
}

plugins {
  kotlin("multiplatform") apply false
  kotlin("android") apply false
  id("com.android.application") apply false
  id("com.android.library") apply false
  id("org.jetbrains.compose") apply false
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
