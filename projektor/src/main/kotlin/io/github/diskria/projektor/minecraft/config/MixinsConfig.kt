package io.github.diskria.projektor.minecraft.config

import io.github.diskria.projektor.gradle.extensions.mappers.toInt
import io.github.diskria.projektor.minecraft.SourceSet
import io.github.diskria.projektor.projekt.MinecraftMod
import io.github.diskria.utils.kotlin.extensions.appendPackageName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MixinsConfig(
    @SerialName("required")
    val isRequired: Boolean,

    @SerialName("package")
    val packageName: String,

    @SerialName("compatibilityLevel")
    val javaTargetVersion: String,

    @SerialName("injectors")
    val injectorConfig: InjectorConfig,

    @SerialName("overwrites")
    val overwriteConfig: OverwriteConfig,

    @SerialName("mixins")
    val mainMixins: List<String>? = null,

    @SerialName("client")
    val clientMixins: List<String>? = null,

    @SerialName("server")
    val serverMixins: List<String>? = null,
) {
    @Serializable
    data class InjectorConfig(
        val defaultRequire: Int,
    )

    @Serializable
    data class OverwriteConfig(
        val requireAnnotations: Boolean,
    )

    companion object {
        fun of(mod: MinecraftMod, mixins: Map<SourceSet, List<String>>): MixinsConfig {
            val javaTargetVersion = "JAVA_${mod.jvmTarget.toInt()}"
            return MixinsConfig(
                isRequired = true,
                packageName = mod.packageName.appendPackageName("mixins"),
                javaTargetVersion = javaTargetVersion,
                injectorConfig = InjectorConfig(
                    defaultRequire = 1
                ),
                overwriteConfig = OverwriteConfig(
                    requireAnnotations = true
                ),
                mainMixins = mixins[SourceSet.MAIN],
                clientMixins = mixins[SourceSet.CLIENT]?.map { "client.$it" },
                serverMixins = mixins[SourceSet.SERVER]?.map { "server.$it" },
            )
        }
    }
}
