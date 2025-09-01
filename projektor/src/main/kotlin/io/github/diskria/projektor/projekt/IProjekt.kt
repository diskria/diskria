package io.github.diskria.projektor.projekt

import io.github.diskria.projektor.licenses.License
import io.github.diskria.projektor.owner.ProjektOwner
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

interface IProjekt {
    val owner: ProjektOwner
    val license: License
    val name: String
    val description: String
    val version: String
    val slug: String
    val packageName: String
    val packagePath: String
    val className: String
    val javaVersion: Int
    val jvmTarget: JvmTarget
    val kotlinVersion: String
    val scm: ScmType
    val softwareForge: SoftwareForgeType
}
