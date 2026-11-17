pluginManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/") // Google mirror of Maven Central, avoids rate limits
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/") // Google mirror of Maven Central, avoids rate limits
        mavenCentral()
    }
}

rootProject.name = "toolkit"
include(":compose")
include(":core")
include(":koin")
if (gradle.parent == null) {
    include(":sample")
    include(":sample:androidApp")
    include(":sample:composeApp")
}
