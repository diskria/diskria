package io.github.diskria.projektor.projekt

class MinecraftMod(val modrinthProjectUrl: String, private val delegate: IProjekt) : IProjekt by delegate {
    val id: String get() = slug
}
