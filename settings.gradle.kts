pluginManagement {
    repositories {
        google()
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
