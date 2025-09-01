package io.github.diskria.projektor.minecraft

enum class ModEnvironment(
    val fabricConfigValue: String,
    val forgeConfigValue: String,
    val sides: List<ModSide>,
) {
    CLIENT_SERVER(
        "*",
        "BOTH",
        listOf(ModSide.CLIENT, ModSide.SERVER)
    ),
    CLIENT_SIDE_ONLY(
        "client",
        "CLIENT",
        listOf(ModSide.CLIENT)
    ),
    SERVER_SIDE_ONLY(
        "server",
        "SERVER",
        listOf(ModSide.SERVER)
    );

    fun getSourceSets(): List<SourceSet> =
        listOf(SourceSet.MAIN) + sides.map { it.sourceSet }
}
