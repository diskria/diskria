package io.github.diskria.projektor.projekt

import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.generics.joinToString

enum class ScmType(val logicalName: String) {

    GIT("git");

    fun buildUri(vararg parts: String): String =
        listOf("scm", logicalName, *parts).joinToString(Constants.Char.COLON)
}
