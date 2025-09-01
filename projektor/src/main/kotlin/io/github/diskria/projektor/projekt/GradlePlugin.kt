package io.github.diskria.projektor.projekt

class GradlePlugin(private val delegate: IProjekt) : IProjekt by delegate {
    val id: String get() = packageName
}
