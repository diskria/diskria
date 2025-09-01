package io.github.diskria.projektor.owner

sealed class GithubProfile(val username: String) : GithubOwner(username)
