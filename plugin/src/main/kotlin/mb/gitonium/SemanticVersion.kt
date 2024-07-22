package mb.gitonium

/**
 * A semantic version number.
 *
 * This follows the specification of [SemVer](https://semver.org/) version 2.0.0.
 *
 * ## Syntax
 * The basic shape of a version string is:
 *
 * ```
 * major.minor.patch[-prerelease][+build]
 * ```
 *
 * - Whitespace is not allowed anywhere in the version string.
 * - Non-alphanumeric and non-ASCII characters, except for hyphen, plus, and period, are not allowed.
 * - The major, minor, and patch versions:
 *   - may be any integer 0 through [Int.MAX_VALUE];
 *   - are required (except when using non-strict parsing, in which case only the major version is required);
 *   - are represented as a base-10 integer;
 *   - cannot have leading zeros (except when using non-strict parsing).
 * - The pre-release identifiers, if present:
 *   - are separated by periods;
 *   - are case-sensitive;
 *   - may contain ASCII alphanumerics and hyphens;
 *   - may not be empty;
 *   - may not have leading zeros (except when using non-strict parsing).
 * - The build metadata identifiers, if present:
 *   - are separated by periods;
 *   - are case-sensitive;
 *   - may contain ASCII alphanumerics and hyphens;
 *   - may not be empty.
 *   - may have leading zeros.
 */
data class SemanticVersion(
    /** The major version number. */
    val major: Int,
    /** The minor version number; or 0 if not specified. */
    val minor: Int,
    /** The patch version number; or 0 if not specified. */
    val patch: Int,
    /** The pre-release identifiers; or an empty list if not specified. */
    val preRelease: List<String> = emptyList(),
    /** The build identifiers; or an empty list if not specified. */
    val build: List<String> = emptyList(),
): Comparable<SemanticVersion> {

    init {
        require(major >= 0) { "Major version must be non-negative." }
        require(minor >= 0) { "Minor version must be non-negative." }
        require(patch >= 0) { "Patch version must be non-negative." }
        require(preRelease.all { it.isNotEmpty() }) { "Pre-release identifier cannot be an empty string." }
        require(preRelease.all { isValidIdentifier(it, forbidLeadingZeros = true) }) { "Pre-release identifier is not valid or has leading zeros." }
        require(build.all { it.isNotEmpty() }) { "Build identifier cannot be an empty string." }
        require(build.all { isValidIdentifier(it, forbidLeadingZeros = false) }) { "Build identifier is not valid." }
    }

    companion object {

        /**
         * Determines whether the version string is a valid semantic version.
         *
         * @param version The version string to check.
         * @param strict Whether to parse strictly according to the SemVer specification.
         * @return `true` if the version string is a valid semantic version; otherwise, `false`.
         */
        fun isValid(version: String, strict: Boolean = false): Boolean {
            return of(version, strict) != null
        }

        /**
         * Determines whether the identifier is a valid pre-release or build identifier.
         *
         * Such identifiers can only consist of ASCII alphanumeric characters and hyphens.
         * If the identifier consists of only digits and [forbidLeadingZeros] is `true`, it cannot have leading zeros.
         *
         * @param identifier The identifier to check.
         * @param forbidLeadingZeros Whether leading zeros are forbidden in numeric identifiers.
         * @return `true` if the identifier is valid; otherwise, `false`.
         */
        private fun isValidIdentifier(identifier: String, forbidLeadingZeros: Boolean = false): Boolean {
            if (identifier.isEmpty()) return false
            val hasLeadingZero = identifier.startsWith('0')
            var isNumeric = true
            for (c in identifier) {
                if (c !in '0' .. '9') {
                    isNumeric = false
                    if (c !in 'a'..'z' && c !in 'A'..'Z' && c != '-') {
                        // Invalid character
                        return false
                    }
                }
            }
            return !forbidLeadingZeros || !hasLeadingZero || !isNumeric || identifier == "0"
        }

        /**
         * Determines whether the number is a major, minor, or patch version number (without leading zeros).
         *
         * @param number The number to check.
         * @return `true` if the number is valid; otherwise, `false`.
         */
        private fun isValidNumber(number: String): Boolean {
            if (number.isEmpty()) return false
            val hasLeadingZero = number.startsWith('0')
            for (c in number) {
                if (c !in '0' .. '9') {
                    return false
                }
            }
            return !hasLeadingZero || number == "0"
        }

        /**
         * Parses a version string into a deconstructed semantic version.
         *
         * When [strict] is `false, this function is a bit more relaxed than the SemVer specification,
         * allowing for a missing minor and patch version and leading zeros for the major, minor, patch,
         * and pre-release versions numbers. In this case, this normalizes the version number.
         *
         * @param versionStr The version string to parse.
         * @param strict Whether to parse strictly according to the SemVer specification.
         * @return The deconstructed semantic version; or `null` if parsing failed.
         */
        fun of(versionStr: String, strict: Boolean = false): SemanticVersion? {
            var index = 0       // The current index at which we're parsing

            fun parseUpToAny(vararg c: Char): String {
                val end = versionStr.indexOfAny(c, startIndex = index)
                if (end == -1) {
                    val str = versionStr.substring(index)
                    index = versionStr.length
                    return str
                } else {
                    val str = versionStr.substring(index, end)
                    index = end
                    return str
                }
            }

            fun String.trimLeadingZeros(): String {
                if (isEmpty()) return this
                return trimStart('0').ifEmpty { "0" }
            }

            fun String.trimLeadingZerosIfNumeric(): String {
                if (any { it !in '0'..'9' }) return this
                return trimLeadingZeros()
            }

            // Parse the major version
            var majorStr = parseUpToAny('.', '+', '-')
            majorStr = if (!strict) majorStr.trimLeadingZeros() else majorStr
            if (!isValidNumber(majorStr)) return null
            val major: Int = majorStr.toIntOrNull() ?: return null
            assert(major >= 0) // This is always true, since we parsed up to a minus sign if it was there

            // Parse the minor version
            val minor: Int
            if (index < versionStr.length && versionStr[index] == '.') {
                index += 1
                var minorStr = parseUpToAny('.', '+', '-')
                minorStr = if (!strict) minorStr.trimLeadingZeros() else minorStr
                if (!isValidNumber(minorStr)) return null
                minor = minorStr.toIntOrNull() ?: return null
                assert(minor >= 0) // This is always true, since we parsed up to a minus sign if it was there
            } else {
                if (strict) return null
                minor = 0
            }

            // Parse the patch version
            val patch: Int
            if (index < versionStr.length && versionStr[index] == '.') {
                index += 1
                var patchStr = parseUpToAny('+', '-')
                patchStr = if (!strict) patchStr.trimLeadingZeros() else patchStr
                if (!isValidNumber(patchStr)) return null
                patch = patchStr.toIntOrNull() ?: return null
                assert(patch >= 0) // This is always true, since we parsed up to a minus sign if it was there
            } else {
                if (strict) return null
                patch = 0
            }

            // Parse the pre-release identifiers
            val preRelease = mutableListOf<String>()
            if (index < versionStr.length && versionStr[index] == '-') {
                index += 1
                if (index == versionStr.length) return null
                while (index < versionStr.length) {
                    var identifier = parseUpToAny('.', '+')
                    identifier = if (!strict) identifier.trimLeadingZerosIfNumeric() else identifier
                    if (!isValidIdentifier(identifier, forbidLeadingZeros = strict)) return null
                    preRelease.add(identifier)
                    if (index >= versionStr.length || versionStr[index] != '.') break
                    index += 1
                }
            }

            // Parse the build identifiers
            val build = mutableListOf<String>()
            if (index < versionStr.length && versionStr[index] == '+') {
                index += 1
                if (index == versionStr.length) return null
                while (index < versionStr.length) {
                    val identifier = parseUpToAny('.')
                    if (!isValidIdentifier(identifier, forbidLeadingZeros = false)) return null
                    build.add(identifier)
                    if (index >= versionStr.length || versionStr[index] != '.') break
                    index += 1
                }
            }

            if (index < versionStr.length) {
                // We have left-over characters that we didn't parse.
                return null
            }

            return SemanticVersion(major, minor, patch, preRelease, build)
        }
    }

    /** Note: versions that differ only in [build] metadata are considered different. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SemanticVersion) return false
        return major == other.major
          && minor == other.minor
          && patch == other.patch
          && preRelease == other.preRelease
          && build == other.build
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + preRelease.hashCode()
        result = 31 * result + build.hashCode()
        return result
    }

    /** Note: versions that differ only in [build] metadata are considered equal. */
    override fun compareTo(other: SemanticVersion): Int {
        // Precedence is determined by the first difference when comparing each of these identifiers
        // from left to right as follows: Major, minor, and patch versions are always compared numerically.
        val majorDiff = this.major - other.major
        if (majorDiff != 0) return majorDiff
        val minorDiff = this.minor - other.minor
        if (minorDiff != 0) return minorDiff
        val patchDiff = this.patch - other.patch
        if (patchDiff != 0) return patchDiff

        // Compare the pre-release identifiers from left to right
        for (i in 0 until minOf(this.preRelease.size, other.preRelease.size)) {
            // Identifiers consisting of only digits are compared numerically
            // Identifiers with letters of hyphens are compared lexically in ASCII sort order
            // Numeric identifiers always have lower precedence than non-numeric identifiers
            val thisIdentifier = this.preRelease[i]
            val otherIdentifier = other.preRelease[i]
            val thisIsNumeric = thisIdentifier.all { it.isDigit() }
            val otherIsNumeric = otherIdentifier.all { it.isDigit() }
            if (thisIsNumeric && otherIsNumeric) {
                val thisNumber = thisIdentifier.toInt()
                val otherNumber = otherIdentifier.toInt()
                val numberDiff = thisNumber - otherNumber
                if (numberDiff != 0) return numberDiff
            } else if (thisIsNumeric) {
                return -1
            } else if (otherIsNumeric) {
                return 1
            } else {
                val identifierDiff = thisIdentifier.compareTo(otherIdentifier)
                if (identifierDiff != 0) return identifierDiff
            }
        }
        // A larger set of pre-release fields has a higher precedence than a smaller set,
        // if all the preceding identifiers are equal
        return this.preRelease.size - other.preRelease.size
    }

    /** Constructs a version string. */
    override fun toString(): String = buildString {
        append(major)
        append('.')
        append(minor)
        append('.')
        append(patch)
        if (preRelease.isNotEmpty()) {
            append('-')
            append(preRelease.first())
            for (identifier in preRelease.drop(1)) {
                append('.')
                append(identifier)
            }
        }
        if (build.isNotEmpty()) {
            append('+')
            append(build.first())
            for (identifier in build.drop(1)) {
                append('.')
                append(identifier)
            }
        }
    }
}
