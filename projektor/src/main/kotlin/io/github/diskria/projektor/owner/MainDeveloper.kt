package io.github.diskria.projektor.owner

import io.github.diskria.utils.kotlin.extensions.common.buildEmail

object MainDeveloper : GithubProfile("diskria") {
    override val email: String = buildEmail(username, "proton.me")
}
