import dev.detekt.gradle.Detekt
import org.gradle.plugins.signing.Sign
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

plugins {
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.detekt) apply true
    alias(libs.plugins.jetbrains.kotlinx.kover)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.iurysouza.modulegraph)
}

dependencies {
    dokka(project(":compose"))
    dokka(project(":core"))
    dokka(project(":koin"))

    subprojects.forEach { subproject ->
        kover(subproject)
    }
}

dokka {
    moduleName.set("Toolkit")
    dokkaPublications.html {
        includes.from("README.md")
    }
}

kover {
    reports {
        total {
            binary {
                file = layout.buildDirectory.file("reports/kover/report.ic")
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(
        "$projectDir/detekt/detekt.yml",
        "$projectDir/detekt/detekt-concrete.yml",
    )
    autoCorrect = true
}

allprojects {
    group = "glass.yasan.toolkit"
    version = "1.7.20"
}

fun Project.configureDetekt() {
    apply(plugin = "dev.detekt")
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "11"
    }
    detekt {
        buildUponDefaultConfig = true
        config.setFrom(
            "$rootDir/config/detekt/detekt.yml",
            "$rootDir/config/detekt/detekt-kepko.yml",
        )
        source.from(
            "src/androidMain/kotlin",
            "src/androidTest/kotlin",
            "src/commonMain/kotlin",
            "src/commonTest/kotlin",
            "src/iosMain/kotlin",
            "src/iosTest/kotlin",
            "src/jvmAndroidMain/kotlin",
            "src/jvmMain/kotlin",
            "src/jvmTest/kotlin"
        )
    }
}

fun Project.configurePublishing() {
    apply(plugin = "com.vanniktech.maven.publish")
    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(
            automaticRelease = true,
            validateDeployment = false,
        )

        signAllPublications()

        pom {
            name.set("Toolkit")
            description.set("Personal development toolkit for Kotlin Multiplatform")
            inceptionYear.set("2025")
            url.set("https://github.com/yasanglass/toolkit/")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("yasanglass")
                    name.set("Yasan Glass")
                    url.set("https://github.com/yasanglass/")
                }
            }
            scm {
                url.set("https://github.com/yasanglass/toolkit/")
                connection.set("scm:git:git://github.com/yasanglass/toolkit.git")
                developerConnection.set("scm:git:ssh://git@github.com/yasanglass/toolkit.git")
            }
        }
    }

    configure<PublishingExtension> {
        repositories.maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/yasan-org/toolkit")
            credentials(PasswordCredentials::class)
        }
    }

    // GitHub Packages rejects re-uploading a file, so re-runs skip publications that are fully there
    // and stop with a clear error on one an earlier run only partly uploaded.
    val gitHubPackagesUsername = providers.gradleProperty("GitHubPackagesUsername")
    val gitHubPackagesPassword = providers.gradleProperty("GitHubPackagesPassword")
    val signingKey = providers.gradleProperty("signingInMemoryKey")
    tasks.withType<PublishToMavenRepository>().configureEach {
        if (name.endsWith("ToGitHubPackagesRepository")) {
            doFirst("Check GitHub Packages") {
                val task = this as PublishToMavenRepository
                val publication = task.publication
                val coordinates = "${publication.groupId}:${publication.artifactId}:${publication.version}"
                val base = "${task.repository.url.toString().trimEnd('/')}/" +
                    "${publication.groupId.replace('.', '/')}/${publication.artifactId}/${publication.version}/" +
                    "${publication.artifactId}-${publication.version}"
                // Signatures are added below, whether or not the publication lists them as artifacts.
                val files = publication.artifacts.filterNot { it.extension.endsWith("asc") }.map { artifact ->
                    base + artifact.classifier?.let { "-$it" }.orEmpty() + ".${artifact.extension}"
                } + "$base.pom" + "$base.module"
                val expected = if (signingKey.isPresent) files + files.map { "$it.asc" } else files
                val auth = "${gitHubPackagesUsername.orNull}:${gitHubPackagesPassword.orNull}"
                val missing = expected.filterNot { url ->
                    val connection = URI(url).toURL().openConnection() as HttpURLConnection
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 30_000
                    connection.setRequestProperty(
                        "Authorization",
                        "Basic ${Base64.getEncoder().encodeToString(auth.toByteArray())}",
                    )
                    val code = try {
                        connection.responseCode
                    } finally {
                        connection.disconnect()
                    }
                    when (code) {
                        200 -> true
                        404 -> false
                        else -> throw GradleException("Could not check $url on GitHub Packages: HTTP $code")
                    }
                }
                when {
                    missing.isEmpty() -> {
                        task.logger.lifecycle("$coordinates is already on GitHub Packages")
                        throw StopExecutionException()
                    }
                    missing.size == expected.size -> Unit
                    else -> throw GradleException(
                        "$coordinates is only partly on GitHub Packages, missing " +
                            missing.joinToString { it.substringAfterLast('/') } +
                            ". Delete that package version on GitHub, then publish again.",
                    )
                }
            }
        }
    }

    tasks.withType<Sign>().configureEach {
        isEnabled = gradle.startParameter.taskNames.none { it.contains("MavenLocal") }
    }
}

fun Project.configureKover() {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

subprojects {
    val isSample = path.contains("sample")

    configureDetekt()
    configureKover()

    if (isSample.not()) {
        apply(plugin = "org.jetbrains.dokka")
        configurePublishing()
    }
}

moduleGraphConfig {
    readmePath.set("${rootDir}/README.md")
    heading.set("### Modules")
    showFullPath.set(false)
    setStyleByModuleType.set(true)
    nestingEnabled.set(true)
}
