package io.github.diskria.projektor.owner

open class BrandOrganization(name: String) : GithubOwner(name) {
    override val email: String = MainDeveloper.email
}
