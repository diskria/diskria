package io.github.diskria.projektor.minecraft.config.dependencies

interface VersionBound {

    override fun toString(): String

    companion object {
        fun inclusive(version: String): InclusiveVersion =
            InclusiveVersion(version)

        fun exclusive(version: String): ExclusiveVersion =
            ExclusiveVersion(version)
    }
}

fun VersionBound.isInclusive(): Boolean =
    this is InclusiveVersion

@JvmInline
value class InclusiveVersion(val value: String) : VersionBound {
    override fun toString(): String = value
}

@JvmInline
value class ExclusiveVersion(val value: String) : VersionBound {
    override fun toString(): String = value
}
