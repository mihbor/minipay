allprojects {
  group = "ltd.mbor"
  version = "0.2-SNAPSHOT"

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

tasks.register<Copy>("copyApk"){
  val source = project(":android").tasks["packageDebug"]
  dependsOn(source)
  from(source)
  include("**.apk")
  into("web/src/jsMain/resources")
  rename{ "minipay.apk" }
}

tasks.register("minidappWithApk"){
  dependsOn("copyApk", ":web:minidappDistribution")
}