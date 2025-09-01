package io.github.diskria.projektor.gradle.utils

import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.common.fileName

object VersionCatalogUtils {

    const val DEFAULT_CATALOG_NAME = "libs"
    const val CATALOG_VERSIONS = "versions"

    fun buildCatalogFileName(catalogName: String): String =
        fileName(catalogName, "versions", Constants.File.Extension.TOML)
}
