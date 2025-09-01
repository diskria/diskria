package io.github.diskria.projektor.gradle.extensions.common

import org.gradle.api.GradleException

fun gradleError(message: String): Nothing =
    throw GradleException(message)
