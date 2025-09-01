package io.github.diskria.projektor.licenses

import io.github.diskria.utils.kotlin.extensions.common.buildUrl
import io.ktor.http.*

sealed class License(val id: String, val displayName: String) {

    fun getUrl(): String =
        buildUrl("opensource.org") {
            path("licenses", id)
        }
}
