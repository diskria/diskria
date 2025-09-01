package io.github.diskria.projektor.minecraft

enum class ModSide(val logicalName: String, val sourceSet: SourceSet, val maxMemoryGigabytes: Int) {

    CLIENT("client", SourceSet.CLIENT, 4),
    SERVER("server", SourceSet.SERVER, 8);

    fun getMaxMemoryJvmArgument(): String =
        "-Xmx${maxMemoryGigabytes}G"
}
