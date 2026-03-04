import java.net.URL
import java.nio.file.Files

// Root Gradle Settings (Dependency + Plugin Mgmt)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            val remoteUrl = "https://raw.githubusercontent.com/leycm/leycm/refs/heads/main/files/libs.version.toml"
            val localFile = file("$rootDir/.gradle/tmp-libs.versions.toml")

            println("[✓] Loading global libs.versions.toml ...")
            localFile.parentFile.mkdirs()
            if (localFile.exists()) localFile.delete()

            @Suppress("DEPRECATION")
            URL(remoteUrl).openStream().use { input ->
                Files.copy(input, localFile.toPath())
            }

            from(files(localFile))
        }
    }
}

// Project Includes
rootProject.name = "linguae"

include("api", "common")

project(":api").projectDir = file("lng-api")
project(":common").projectDir = file("lng-common")
