package io.github.diskria.projektor.projekt

import io.github.diskria.utils.kotlin.extensions.setCase
import io.github.diskria.utils.kotlin.words.CamelCase
import io.github.diskria.utils.kotlin.words.ScreamingSnakeCase
import kotlin.properties.ReadOnlyProperty

object Secrets {

    val githubPackagesToken: String? by environmentVariableDelegate()
    val gpgKey: String? by environmentVariableDelegate()
    val gpgPassphrase: String? by environmentVariableDelegate()

    private fun environmentVariableDelegate(): ReadOnlyProperty<Any?, String?> =
        ReadOnlyProperty { _, property ->
            System.getenv(
                property
                    .name
                    .takeIf { it.isNotBlank() }
                    ?.setCase(CamelCase, ScreamingSnakeCase)
            )
        }
}