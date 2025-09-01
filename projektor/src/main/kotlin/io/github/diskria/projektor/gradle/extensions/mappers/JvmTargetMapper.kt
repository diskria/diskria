package io.github.diskria.projektor.gradle.extensions.mappers

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun JvmTarget.toInt(): Int =
    if (this == JvmTarget.JVM_1_8) 8
    else target.toInt()

fun Int.toJvmTarget(): JvmTarget =
    if (this == 8) JvmTarget.JVM_1_8
    else JvmTarget.fromTarget(toString())
