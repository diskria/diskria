package io.github.diskria.projektor.minecraft.config.dependencies

import io.github.diskria.utils.kotlin.BracketsType
import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.common.failWithWrongUsage
import io.github.diskria.utils.kotlin.extensions.common.unsupportedOperation
import io.github.diskria.utils.kotlin.extensions.wrapWithBrackets

sealed class VersionRange {

    abstract val any: String

    protected abstract fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String

    fun min(version: VersionBound): String =
        rangeInternal(version, null)

    fun max(version: VersionBound): String =
        rangeInternal(null, version)

    fun range(minVersion: VersionBound? = null, maxVersion: VersionBound? = null): String {
        if (minVersion == null && maxVersion == null) {
            failWithWrongUsage(useInsteadThis = "any")
        }
        return rangeInternal(minVersion, maxVersion)
    }
}

object FabricVersionRange : VersionRange() {

    override val any: String = Constants.Char.ASTERISK.toString()

    override fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String {
        val min = if (minVersion == null) null else buildString {
            append(Constants.Char.CLOSING_ANGLE_BRACKET)
            if (minVersion.isInclusive()) {
                append(Constants.Char.EQUAL_SIGN)
            }
            append(minVersion.toString())
        }
        val max = if (maxVersion == null) null else buildString {
            append(Constants.Char.OPENING_ANGLE_BRACKET)
            if (maxVersion.isInclusive()) {
                append(Constants.Char.EQUAL_SIGN)
            }
            append(maxVersion.toString())
        }
        return when {
            min != null && max != null -> min + Constants.Char.SPACE + max
            min != null -> min
            max != null -> max
            else -> unsupportedOperation()
        }
    }
}

object ForgeVersionRange : VersionRange() {

    private const val SEPARATOR: Char = Constants.Char.COMMA
    private val EXCLUSIVE_BRACKETS_TYPE: BracketsType = BracketsType.ROUND

    override val any: String = SEPARATOR.toString().wrapWithBrackets(EXCLUSIVE_BRACKETS_TYPE)

    override fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String {
        val min = when (minVersion) {
            null -> EXCLUSIVE_BRACKETS_TYPE.openingChar.toString()
            else -> buildString {
                val bracketsType = getBracketsType(minVersion.isInclusive())
                append(bracketsType.openingChar)
                append(minVersion.toString())
            }
        }
        val max = when (maxVersion) {
            null -> EXCLUSIVE_BRACKETS_TYPE.closingChar.toString()
            else -> buildString {
                val bracketsType = getBracketsType(maxVersion.isInclusive())
                append(maxVersion.toString())
                append(bracketsType.closingChar)
            }
        }
        return min + SEPARATOR + max
    }

    private fun getBracketsType(isInclusive: Boolean): BracketsType =
        if (isInclusive) BracketsType.SQUARE
        else EXCLUSIVE_BRACKETS_TYPE
}
