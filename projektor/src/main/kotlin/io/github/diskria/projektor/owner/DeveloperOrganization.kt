package io.github.diskria.projektor.owner

import io.github.diskria.utils.kotlin.Constants

open class DeveloperOrganization(
    profile: GithubProfile,
    name: String,
) : Organization(profile.username + Constants.Char.HYPHEN + name) {
    override val namespace: String = profile.namespace
    override val email: String = profile.email
}
