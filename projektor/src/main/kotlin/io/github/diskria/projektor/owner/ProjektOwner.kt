package io.github.diskria.projektor.owner

import io.github.diskria.projektor.projekt.SoftwareForgeType

sealed class ProjektOwner(val name: String, val softwareForgeType: SoftwareForgeType) {

    abstract val namespace: String
    abstract val email: String

    abstract fun getRepositoryUrl(slug: String, isVcsUrl: Boolean = false): String
    abstract fun getRepositoryPath(slug: String, isVcsUrl: Boolean = false): String
    abstract fun getIssuesUrl(slug: String): String
}
