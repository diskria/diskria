package io.github.diskria.projektor.minecraft.config

import io.github.diskria.projektor.gradle.extensions.mappers.toInt
import io.github.diskria.projektor.minecraft.ModEnvironment
import io.github.diskria.projektor.minecraft.config.dependencies.FabricVersionRange
import io.github.diskria.projektor.minecraft.config.dependencies.VersionBound
import io.github.diskria.projektor.minecraft.config.dependencies.VersionRange
import io.github.diskria.projektor.owner.MainDeveloper
import io.github.diskria.projektor.projekt.MinecraftMod
import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.appendPackageName
import io.github.diskria.utils.kotlin.extensions.common.fileName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FabricModConfig(
    val schemaVersion: Int,
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val authors: List<String>,
    val license: String,
    val icon: String,
    val environment: String,
    val mixins: List<String>,
    val accessWidener: String,

    @SerialName("contact")
    val links: Links,

    @SerialName("entrypoints")
    val entryPoints: EntryPoints,

    @SerialName("depends")
    val dependencies: Dependencies,
) {
    @Serializable
    class Links private constructor(
        @SerialName("homepage")
        val modrinthProjectUrl: String,

        @SerialName("sources")
        val sourceCodeUrl: String,
    ) {
        companion object {
            fun of(modrinthProjectUrl: String, sourceCodeUrl: String): Links =
                Links(
                    modrinthProjectUrl = modrinthProjectUrl,
                    sourceCodeUrl = sourceCodeUrl,
                )
        }
    }

    @Serializable
    class EntryPoints(
        @SerialName("main")
        val mainEntryPoints: List<EntryPoint>? = null,

        @SerialName("client")
        val clientEntryPoints: List<EntryPoint>? = null,

        @SerialName("server")
        val serverEntryPoints: List<EntryPoint>? = null,

        @SerialName("fabric-datagen")
        val dataGenerators: List<EntryPoint>? = null,
    ) {
        companion object {
            fun of(mod: MinecraftMod, environment: ModEnvironment, dataGenerators: List<String>): EntryPoints {
                val mainEntryPoints = entryPoints(mod.packageName.appendPackageName(mod.className + "Mod"))
                val clientEntryPoints = entryPoints(mod.packageName.appendPackageName(mod.className + "Client"))
                val serverEntryPoints = entryPoints(mod.packageName.appendPackageName(mod.className + "Server"))
                val dataGeneratorEntryPoints = dataGenerators.map { EntryPoint.of(it) }.ifEmpty { null }

                return when (environment) {
                    ModEnvironment.CLIENT_SERVER -> EntryPoints(
                        mainEntryPoints = mainEntryPoints,
                        clientEntryPoints = clientEntryPoints,
                        serverEntryPoints = serverEntryPoints,
                        dataGenerators = dataGeneratorEntryPoints,
                    )

                    ModEnvironment.CLIENT_SIDE_ONLY -> EntryPoints(
                        clientEntryPoints = clientEntryPoints,
                        dataGenerators = dataGeneratorEntryPoints,
                    )

                    ModEnvironment.SERVER_SIDE_ONLY -> EntryPoints(
                        serverEntryPoints = serverEntryPoints
                    )
                }
            }

            private fun entryPoints(vararg classPaths: String): List<EntryPoint> =
                classPaths.map { EntryPoint.of(it) }
        }
    }

    @Serializable
    class EntryPoint(
        @SerialName("adapter")
        val language: String,

        @SerialName("value")
        val classPath: String,
    ) {
        companion object {
            fun of(classPath: String): EntryPoint =
                EntryPoint(
                    classPath = classPath,
                    language = "kotlin",
                )
        }
    }

    @Serializable
    class Dependencies(
        @SerialName("java")
        val jvmDependency: String,

        @SerialName("minecraft")
        val minecraftDependency: String,

        @SerialName("fabricloader")
        val loaderDependency: String,

        @SerialName("fabric-language-kotlin")
        val kotlinDependency: String,

        @SerialName("fabric-api")
        val apiDependency: String?,
    ) {
        companion object {
            fun of(
                javaVersion: Int,
                minecraftVersion: String,
                loaderVersion: String,
                isApiRequired: Boolean,
                versionRange: VersionRange = FabricVersionRange,
            ): Dependencies =
                Dependencies(
                    jvmDependency = versionRange.min(VersionBound.inclusive(javaVersion.toString())),
                    minecraftDependency = versionRange.min(VersionBound.inclusive(minecraftVersion)),
                    loaderDependency = versionRange.min(VersionBound.inclusive(loaderVersion)),
                    kotlinDependency = versionRange.any,
                    apiDependency = versionRange.any.takeIf { isApiRequired },
                )
        }
    }

    companion object {
        fun of(
            mod: MinecraftMod,
            environment: ModEnvironment,
            minecraftVersion: String,
            loaderVersion: String,
            isApiRequired: Boolean,
            dataGenerators: List<String>,
        ): FabricModConfig =
            FabricModConfig(
                schemaVersion = 1,
                id = mod.id,
                version = mod.version,
                name = mod.name,
                description = mod.description,
                authors = listOf(MainDeveloper.name),
                license = mod.license.id,
                icon = "assets/${mod.slug}/${fileName("icon", "png")}",
                environment = environment.fabricConfigValue,
                accessWidener = fileName(mod.slug, "accesswidener"),
                mixins = listOf(fileName(mod.slug, "mixins", Constants.File.Extension.JSON)),
                links = Links.of(
                    modrinthProjectUrl = mod.modrinthProjectUrl,
                    sourceCodeUrl = mod.owner.getRepositoryUrl(mod.slug),
                ),
                entryPoints = EntryPoints.of(
                    mod,
                    environment,
                    dataGenerators,
                ),
                dependencies = Dependencies.of(
                    mod.jvmTarget.toInt(),
                    minecraftVersion,
                    loaderVersion,
                    isApiRequired,
                ),
            )
    }
}
