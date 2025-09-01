package io.github.diskria.projektor.owner

import io.github.diskria.projektor.projekt.SoftwareForgeType
import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.appendPrefix
import io.github.diskria.utils.kotlin.extensions.appendSuffix
import io.github.diskria.utils.kotlin.extensions.common.modifyIf
import io.github.diskria.utils.kotlin.extensions.removePrefix
import io.ktor.http.*

abstract class GithubOwner(name: String) : ProjektOwner(name, SoftwareForgeType.GITHUB) {

    abstract override val email: String

    override val namespace: String = "io.${softwareForgeType.displayName.lowercase()}.${name.lowercase()}"

    fun getPackagesMavenUrl(slug: String): String =
        getRepositoryUrlBuilder(slug, isMaven = true).build().toString()

    override fun getRepositoryUrl(slug: String, isVcsUrl: Boolean): String =
        getRepositoryUrlBuilder(slug, isVcsUrl).build().toString()

    override fun getRepositoryPath(slug: String, isVcsUrl: Boolean): String =
        getRepositoryUrlBuilder(slug, isVcsUrl = isVcsUrl).build().encodedPath.removePrefix(Constants.Char.SLASH)

    override fun getIssuesUrl(slug: String): String =
        getRepositoryUrlBuilder(slug).apply {
            path("issues")
        }.build().toString()

    private fun getRepositoryUrlBuilder(slug: String, isVcsUrl: Boolean = false, isMaven: Boolean = false): URLBuilder =
        URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = softwareForgeType.hostname.modifyIf(isMaven) { it.appendPrefix("maven.pkg.") }
            path(name, slug.modifyIf(isVcsUrl) { it.appendSuffix(".${softwareForgeType.scmType.logicalName}") })
        }
}
