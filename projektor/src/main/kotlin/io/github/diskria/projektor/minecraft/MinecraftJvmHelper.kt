package io.github.diskria.projektor.minecraft

import io.github.diskria.projektor.gradle.extensions.mappers.toJvmTarget
import io.github.diskria.utils.kotlin.delegates.toAutoNamedProperty
import io.github.diskria.utils.kotlin.extensions.common.failWithDetails
import io.github.diskria.utils.kotlin.extensions.toSemver
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object MinecraftJvmHelper {

    private val minecraftToJava: Map<String, Int> =
        mapOf(
            "1.20.5" to 21,
            "1.18" to 17,
            "1.17" to 16,
            MinecraftConstants.MIN_SUPPORTED_VERSION to 8,
        )

    fun getJvmTarget(minecraftVersion: String): JvmTarget {
        val targetSemver = minecraftVersion.toSemver()
        return minecraftToJava
            .mapKeys { it.key.toSemver() }
            .filterKeys { it <= targetSemver }
            .maxByOrNull { it.key }
            ?.value
            ?.toJvmTarget()
            ?: failWithDetails("Too old Minecraft version") {
                val requiredVersion by minecraftVersion.toAutoNamedProperty()
                val minSupportedVersion by MinecraftConstants.MIN_SUPPORTED_VERSION.toAutoNamedProperty()
                listOf(requiredVersion, minSupportedVersion)
            }
    }
}
