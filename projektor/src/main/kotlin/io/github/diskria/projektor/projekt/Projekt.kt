package io.github.diskria.projektor.projekt

import io.github.diskria.projektor.licenses.License
import io.github.diskria.projektor.owner.ProjektOwner
import io.github.diskria.utils.kotlin.extensions.setCase
import io.github.diskria.utils.kotlin.words.DotCase
import io.github.diskria.utils.kotlin.words.PascalCase
import io.github.diskria.utils.kotlin.words.PathCase
import io.github.diskria.utils.kotlin.words.SpaceCase
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

data class Projekt(
    override val owner: ProjektOwner,
    override val license: License,
    override val name: String,
    override val description: String,
    override val version: String,
    override val slug: String,
    override val packageName: String,
    override val packagePath: String = packageName.setCase(DotCase, PathCase),
    override val className: String = name.setCase(SpaceCase, PascalCase),
    override val javaVersion: Int,
    override val jvmTarget: JvmTarget,
    override val kotlinVersion: String,
    override val scm: ScmType = ScmType.GIT,
    override val softwareForge: SoftwareForgeType = owner.softwareForgeType,
) : IProjekt {

    fun toGradlePlugin(): GradlePlugin =
        GradlePlugin(this)

    fun toLibrary(): Library =
        Library(this)

    fun toMinecraftMod(modrinthProjectUrl: String): MinecraftMod =
        MinecraftMod(modrinthProjectUrl, this)

    fun toAndroidApp(): AndroidApp =
        AndroidApp(this)
}
