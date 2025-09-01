fun RepositoryHandler.mavenGradlePluginPortal() {
    maven("https://plugins.gradle.org/m2")
}

fun RepositoryHandler.mavenFabricMinecraft() {
    maven("https://maven.fabricmc.net")
}

fun RepositoryHandler.mavenForgeMinecraft() {
    maven("https://maven.minecraftforge.net")
}

fun RepositoryHandler.commonRepositories() {
    mavenLocal()
    mavenCentral()
    mavenGradlePluginPortal()
    mavenFabricMinecraft()
    mavenForgeMinecraft()
}

fun RepositoryHandler.pluginRepositories() {
    gradlePluginPortal()
}

@Suppress("UnstableApiUsage")
fun setupRepositories() {
    dependencyResolutionManagement.repositories {
        commonRepositories()
    }

    pluginManagement.repositories {
        commonRepositories()
        pluginRepositories()
    }
}

setupRepositories()

fun setupVersionCatalogs() {
    dependencyResolutionManagement
        .versionCatalogs
        .create("libs")
        .from(files("../gradle/libs.versions.toml"))
}

setupVersionCatalogs()
