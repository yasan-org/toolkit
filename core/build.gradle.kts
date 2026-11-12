import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val artifactId = "core"

plugins {
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    explicitApi()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }

    android {
        namespace = "glass.yasan.toolkit.$artifactId"
        compileSdk = libs.versions.android.sdk.compile.get().toInt()
        minSdk = libs.versions.android.sdk.min.get().toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js {
        browser()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.core)
                implementation(libs.ktor.http)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test)
            }
        }
        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain {
            dependsOn(nonWebMain)
        }
        androidMain {
            dependsOn(nonWebMain)
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.android)
                implementation(libs.androidx.browser)
            }
        }
        iosMain {
            dependsOn(nonWebMain)
        }
        macosMain {
            dependsOn(nonWebMain)
        }
    }
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    coordinates(artifactId = artifactId)
}
