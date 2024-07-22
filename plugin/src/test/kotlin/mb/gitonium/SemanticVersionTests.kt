package mb.gitonium

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeNegative
import io.kotest.matchers.ints.shouldBePositive
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/** Tests the [SemanticVersion] class. */
class SemanticVersionTests: FunSpec({

    /** Test cases that are always valid. */
    val validTests = listOf(
        // NOTE: These test cases must be ordered (for compareTo() tests).
        // NOTE: There must be no duplicates in these test cases (for equals() tests).
        // From: https://regex101.com/r/vkijKf/1/
        "0.0.4" to SemanticVersion(0, 0, 4),
        "1.0.0" to SemanticVersion(1, 0, 0),
        "1.0.0+0.build.1-rc.10000aaa-kk-0.1" to SemanticVersion(1, 0, 0, emptyList(), listOf("0", "build", "1-rc", "10000aaa-kk-0", "1")),
        "1.0.0-0A.is.legal" to SemanticVersion(1, 0, 0, listOf("0A", "is", "legal")),
        "1.0.0-alpha" to SemanticVersion(1, 0, 0, listOf("alpha")),
        "1.0.0-alpha+beta" to SemanticVersion(1, 0, 0, listOf("alpha"), listOf("beta")),
        "1.0.0-alpha.1" to SemanticVersion(1, 0, 0, listOf("alpha", "1")),
        "1.0.0-alpha.0valid" to SemanticVersion(1, 0, 0, listOf("alpha", "0valid")),
        "1.0.0-alpha.beta" to SemanticVersion(1, 0, 0, listOf("alpha", "beta")),
        "1.0.0-alpha.beta.1" to SemanticVersion(1, 0, 0, listOf("alpha", "beta", "1")),
        "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay" to SemanticVersion(1, 0, 0, listOf("alpha-a", "b-c-somethinglong"), listOf("build", "1-aef", "1-its-okay")),
        "1.0.0-alpha0.valid" to SemanticVersion(1, 0, 0, listOf("alpha0", "valid")),
        "1.0.0-beta" to SemanticVersion(1, 0, 0, listOf("beta")),
        "1.0.0-rc.1+build.1" to SemanticVersion(1, 0, 0, listOf("rc", "1"), listOf("build", "1")),
        "1.1.2+meta" to SemanticVersion(1, 1, 2, emptyList(), listOf("meta")),
        "1.1.2+meta-valid" to SemanticVersion(1, 1, 2, emptyList(), listOf("meta-valid")),
        "1.1.2-prerelease+meta" to SemanticVersion(1, 1, 2, listOf("prerelease"), listOf("meta")),
        "1.1.7" to SemanticVersion(1, 1, 7),
        "1.2.3" to SemanticVersion(1, 2, 3),
        "1.2.3----R-S.12.9.1--.12+meta" to SemanticVersion(1, 2, 3, listOf("---R-S", "12", "9", "1--", "12"), listOf("meta")),
        "1.2.3----RC-SNAPSHOT.12.9.1--.12" to SemanticVersion(1, 2, 3, listOf("---RC-SNAPSHOT", "12", "9", "1--", "12")),
        "1.2.3----RC-SNAPSHOT.12.9.1--.12+788" to SemanticVersion(1, 2, 3, listOf("---RC-SNAPSHOT", "12", "9", "1--", "12"), listOf("788")),
        "1.2.3-SNAPSHOT-123" to SemanticVersion(1, 2, 3, listOf("SNAPSHOT-123")),
        "1.2.3-beta" to SemanticVersion(1, 2, 3, listOf("beta")),
        "2.0.0" to SemanticVersion(2, 0, 0),
        "2.0.0+build.1848" to SemanticVersion(2, 0, 0, emptyList(), listOf("build", "1848")),
        "2.0.0+build.001848" to SemanticVersion(2, 0, 0, emptyList(), listOf("build", "001848")),
        "2.0.0+001848" to SemanticVersion(2, 0, 0, emptyList(), listOf("001848")),
        "2.0.0-rc.1+build.123" to SemanticVersion(2, 0, 0, listOf("rc", "1"), listOf("build", "123")),
        "2.0.1-alpha.1227" to SemanticVersion(2, 0, 1, listOf("alpha", "1227")),
        "10.2.3-DEV-SNAPSHOT" to SemanticVersion(10, 2, 3, listOf("DEV-SNAPSHOT")),
        "10.20.30" to SemanticVersion(10, 20, 30),
    )

    /** Test cases that are only valid in non-strict mode. */
    val validNonStrictTests = listOf(
        // NOTE: These test cases must be ordered (for compareTo() tests).
        // Also from: https://regex101.com/r/vkijKf/1/
        "0" to SemanticVersion(0, 0, 0),
        "0.0" to SemanticVersion(0, 0, 0),
        "1" to SemanticVersion(1, 0, 0),
        "01.1.1" to SemanticVersion(1, 1, 1),
        "1.01.1" to SemanticVersion(1, 1, 1),
        "1.1.01" to SemanticVersion(1, 1, 1),
        "1.2" to SemanticVersion(1, 2, 0),
        "1.2+meta" to SemanticVersion(1, 2, 0, emptyList(), listOf("meta")),
        "1.2-alpha" to SemanticVersion(1, 2, 0, listOf("alpha")),
        "1.2-prerelease+meta" to SemanticVersion(1, 2, 0, listOf("prerelease"), listOf("meta")),
        "01.2.3" to SemanticVersion(1, 2, 3),
        "1.02.3" to SemanticVersion(1, 2, 3),
        "1.2.03" to SemanticVersion(1, 2, 3),
        "1.2.3-0123" to SemanticVersion(1, 2, 3, listOf("123")),
        "1.2.3-0123.0123" to SemanticVersion(1, 2, 3, listOf("123", "123")),
    )

    /** Test cases that are always invalid. */
    val invalidTests = listOf(
        // From: https://regex101.com/r/vkijKf/1/
        "1.1.2+.123",
        "+invalid",
        "-invalid",
        "-invalid+invalid",
        "-invalid.01",
        "alpha",
        "alpha.beta",
        "alpha.beta.1",
        "alpha.1",
        "alpha+beta",
        "alpha_beta",
        "alpha.",
        "alpha..",
        "beta",
        "1.0.0-alpha_beta",
        "-alpha.",
        "1.0.0-alpha..",
        "1.0.0-alpha..1",
        "1.0.0-alpha...1",
        "1.0.0-alpha....1",
        "1.0.0-alpha.....1",
        "1.0.0-alpha......1",
        "1.0.0-alpha.......1",
        "1.2.3.DEV",
        "1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788",
        "-1.0.3-gamma+b7718",
        "+justmeta",
        "9.8.7+meta+meta",
        "9.8.7-whatever+meta+meta",
        // Too long:
        //"99999999999999999999999.999999999999999999.99999999999999999----RC-SNAPSHOT.12.09.1--------------------------------..12",
    )

    data class TestVersion(
        /** The major version number; or `null` if not specified. */
        val major: Int?,
        /** The minor version number; or `null` if not specified. */
        val minor: Int?,
        /** The patch version number; or `null` if not specified. */
        val patch: Int?,
        /** The pre-release identifiers; or an empty list if not specified. */
        val preRelease: List<String>,
        /** The build identifiers; or an empty list if not specified. */
        val build: List<String>,
    ) {
        /** Constructs a version string. */
        override fun toString(): String = buildString {
            // This code should be similar to the code in SemanticVersion.toString()
            //  except that this code allows building invalid version strings (for testing).
            if (major != null) {
                append(major)
            }
            if (minor != null) {
                append('.')
                append(minor)
            }
            if (patch != null) {
                append('.')
                append(patch)
            }
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

    context("constructor") {
        test("should accept valid constructor values") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.list(Arb.stringPattern("([1-9][0-9]*)|([a-zA-Z\\-][a-zA-Z0-9\\-]*)|([0-9]+[a-zA-Z\\-][a-zA-Z0-9\\-]*)")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                SemanticVersion(major, minor, patch, preRelease, build)
            }
        }

        withData(
            nameFn = { (major, minor, patch, preRelease, build) -> "should reject invalid constructor values: (major=$major, minor=$minor, patch=$patch, preRelease=$preRelease, build=$build)"},
            TestVersion(-1, 0, 0, emptyList(), emptyList()),        // Negative major version
            TestVersion(0, -1, 0, emptyList(), emptyList()),        // Negative minor version
            TestVersion(0, 0, -1, emptyList(), emptyList()),        // Negative patch version
            TestVersion(0, 0, 0, listOf(""), emptyList()),          // Empty pre-release identifier
            TestVersion(0, 0, 0, listOf("0123"), emptyList()),      // Pre-release identifier with leading zeros
            TestVersion(0, 0, 0, listOf("foo!"), emptyList()),      // Pre-release identifier with invalid characters
            TestVersion(0, 0, 0, listOf("foo.bar"), emptyList()),   // Pre-release identifier with invalid characters
            TestVersion(0, 0, 0, emptyList(), listOf("")),          // Empty build identifier
            TestVersion(0, 0, 0, emptyList(), listOf("foo!")),      // Build identifier with invalid characters
            TestVersion(0, 0, 0, emptyList(), listOf("foo.bar")),   // Build identifier with invalid characters
        ) { (major, minor, patch, preRelease, build) ->
            // Act/Assert
            shouldThrow<IllegalArgumentException> {
                SemanticVersion(major!!, minor!!, patch!!, preRelease, build)
            }
        }
    }

    context("isValid()") {
        test("should accept valid version strings") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.list(Arb.stringPattern("([1-9][0-9]*)|([a-zA-Z\\-][a-zA-Z0-9\\-]*)|([0-9]+[a-zA-Z\\-][a-zA-Z0-9\\-]*)")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val versionStr = TestVersion(major, minor, patch, preRelease, build).toString()

                // Act
                val isValid = SemanticVersion.isValid(versionStr, strict = true)

                // Assert
                isValid shouldBe true
            }
        }

        test("should accept valid non-strict version strings, when strict is false") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0).orNull(),
                Arb.int(min = 0).orNull(),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val versionStr = TestVersion(major, minor, patch, preRelease, build).toString()

                // Act
                val isValid = SemanticVersion.isValid(versionStr, strict = false)

                // Assert
                isValid shouldBe true
            }
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is true, should accept: \"$versionStr\""},
            validTests,
        ) { (versionStr, _) ->
            // Act
            val isValid = SemanticVersion.isValid(versionStr, strict = true)

            // Assert
            isValid shouldBe true
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is false, should accept: \"$versionStr\""},
            validTests + validNonStrictTests
        ) { (versionStr, _) ->
            // Act
            val isValid = SemanticVersion.isValid(versionStr, strict = false)

            // Assert
            isValid shouldBe true
        }

        withData(
            nameFn = { versionStr -> "when strict is true, should reject: \"$versionStr\""},
            invalidTests + validNonStrictTests.map { it.first },
        ) { versionStr ->
            // Act
            val isValid = SemanticVersion.isValid(versionStr, strict = true)

            // Assert
            isValid shouldBe false
        }

        withData(
            nameFn = { versionStr -> "when strict is false, should reject: \"$versionStr\""},
            invalidTests
        ) { versionStr ->
            // Act
            val isValid = SemanticVersion.isValid(versionStr, strict = false)

            // Assert
            isValid shouldBe false
        }
    }


    context("of()") {
        context("when strict is true") {
            test("should accept 1.0.0") {
                // Arrange
                val versionStr = "1.0.0"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 0, 0)
            }

            test("should accept 1.2.3") {
                // Arrange
                val versionStr = "1.2.3"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3)
            }

            test("should accept 1.2.3-alpha") {
                // Arrange
                val versionStr = "1.2.3-alpha"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha"))
            }

            test("should accept 1.2.3+build") {
                // Arrange
                val versionStr = "1.2.3+build"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, emptyList(), listOf("build"))
            }

            test("should accept 1.2.3-alpha+build") {
                // Arrange
                val versionStr = "1.2.3-alpha+build"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha"), listOf("build"))
            }

            test("should accept 1.2.3-alpha.123+build.0456") {
                // Arrange
                val versionStr = "1.2.3-alpha.123+build.0456"

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha", "123"), listOf("build", "0456"))
            }
        }

        context("when strict is false") {
            test("should accept 1.0.0") {
                // Arrange
                val versionStr = "1.0.0"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 0, 0)
            }

            test("should accept 1") {
                // Arrange
                val versionStr = "1"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 0, 0)
            }

            test("should accept 1.2") {
                // Arrange
                val versionStr = "1.2"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 0)
            }

            test("should accept 1.2.3") {
                // Arrange
                val versionStr = "1.2.3"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3)
            }

            test("should accept 1.2.3-alpha") {
                // Arrange
                val versionStr = "1.2.3-alpha"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha"))
            }

            test("should accept 1.2.3+build") {
                // Arrange
                val versionStr = "1.2.3+build"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, emptyList(), listOf("build"))
            }

            test("should accept 1.2.3-alpha+build") {
                // Arrange
                val versionStr = "1.2.3-alpha+build"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha"), listOf("build"))
            }

            test("should accept 1.2.3-alpha.123+build.0456") {
                // Arrange
                val versionStr = "1.2.3-alpha.123+build.0456"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha", "123"), listOf("build", "0456"))
            }

            test("should accept 1.2.3-alpha.0123+build.0456") {
                // Arrange
                val versionStr = "1.2.3-alpha.0123+build.0456"

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldBe SemanticVersion(1, 2, 3, listOf("alpha", "123"), listOf("build", "0456"))
            }
        }

        test("should accept valid version strings") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.list(Arb.stringPattern("([1-9][0-9]*)|([a-zA-Z\\-][a-zA-Z0-9\\-]*)|([0-9]+[a-zA-Z\\-][a-zA-Z0-9\\-]*)")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val versionStr = TestVersion(major, minor, patch, preRelease, build).toString()

                // Act
                val version = SemanticVersion.of(versionStr, strict = true)

                // Assert
                version shouldNotBe null
            }
        }

        test("should accept valid non-strict version strings, when strict is false") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0).orNull(),
                Arb.int(min = 0).orNull(),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val versionStr = TestVersion(major, minor, patch, preRelease, build).toString()

                // Act
                val version = SemanticVersion.of(versionStr, strict = false)

                // Assert
                version shouldNotBe null
            }
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is true, should accept: \"$versionStr\""},
            validTests,
        ) { (versionStr, expectedVersion) ->
            // Act
            val version = SemanticVersion.of(versionStr, strict = true)

            // Assert
            version shouldBe expectedVersion
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is false, should accept: \"$versionStr\""},
            validTests + validNonStrictTests
        ) { (versionStr, expectedVersion) ->
            // Act
            val version = SemanticVersion.of(versionStr, strict = false)

            // Assert
            version shouldBe expectedVersion
        }

        withData(
            nameFn = { versionStr -> "when strict is true, should reject: \"$versionStr\""},
            invalidTests + validNonStrictTests.map { it.first },
        ) { versionStr ->
            // Act
            val version = SemanticVersion.of(versionStr, strict = true)

            // Assert
            version shouldBe null
        }

        withData(
            nameFn = { versionStr -> "when strict is false, should reject: \"$versionStr\""},
            invalidTests
        ) { versionStr ->
            // Act
            val version = SemanticVersion.of(versionStr, strict = false)

            // Assert
            version shouldBe null
        }
    }

    @Suppress("ReplaceCallWithBinaryOperator")
    context("equals()") {
        withData(
            nameFn = { (a, b) -> "should find \"$a\" != \"$b\""},
            validTests.flatMapIndexed { i, (_, version1) ->
                validTests.take((i - 1).coerceAtLeast(0)).map { (_, version2) -> version1 to version2 } +
                validTests.drop(i + 1).map { (_, version2) -> version1 to version2 }
            },
        ) { (a, b) ->
            // Act
            val ab = a.equals(b)
            val ba = b.equals(a)

            // Assert
            withClue("expected $a != $b") {
                ab.shouldBeFalse()
            }
            withClue("expected $b != $a") {
                ba.shouldBeFalse()
            }
        }

        withData(
            nameFn = { (a, b) -> "should find \"$a\" == \"$b\""},
            validTests.map { (versionStr, version) -> version to SemanticVersion.of(versionStr)!! },
        ) { (a, b) ->
            // Act
            val ab = a.equals(b)
            val ba = b.equals(a)
            val hashA = a.hashCode()
            val hashB = b.hashCode()

            // Assert
            withClue("expected $a == $b") {
                ab.shouldBeTrue()
            }
            withClue("expected $b == $a") {
                ba.shouldBeTrue()
            }
            withClue("expected hash($a) == hash($b)") {
                hashA shouldBe hashB
            }
        }
    }

    context("compareTo()") {
        withData(
            nameFn = { (a, b) -> "should compare \"$a\" <= \"$b\""},
            validTests.flatMapIndexed { i, (_, version1) ->
                validTests.drop(i).map { (_, version2) -> version1 to version2 }
            },
        ) { (a, b) ->
            // Act
            val ab = a.compareTo(b)
            val ba = b.compareTo(a)

            // Assert
            // NOTE: We don't compare the build identifiers.
            if (a.copy(build = emptyList()) != b.copy(build = emptyList())) {
                withClue("expected $a < $b") {
                    ab.shouldBeNegative()
                }
                withClue("expected $b > $a") {
                    ba.shouldBePositive()
                }
            } else {
                withClue("expected $a == $b") {
                    ab.shouldBeZero()
                }
                withClue("expected $b == $a") {
                    ba.shouldBeZero()
                }
            }
        }

        withData(
            nameFn = { (a, b) -> "should compare \"$a\" <= \"$b\""},
            validNonStrictTests.flatMapIndexed { i, (_, version1) ->
                validNonStrictTests.drop(i).map { (_, version2) -> version1 to version2 }
            },
        ) { (a, b) ->
            // Act
            val ab = a.compareTo(b)
            val ba = b.compareTo(a)

            // Assert
            // NOTE: We don't compare the build identifiers.
            if (a.copy(build = emptyList()) != b.copy(build = emptyList())) {
                withClue("expected $a < $b") {
                    ab.shouldBeNegative()
                }
                withClue("expected $b > $a") {
                    ba.shouldBePositive()
                }
            } else {
                withClue("expected $a == $b") {
                    ab.shouldBeZero()
                }
                withClue("expected $b == $a") {
                    ba.shouldBeZero()
                }
            }
        }
    }

    context("toString()") {
        test("should print valid version strings") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.int(min = 0),
                Arb.list(Arb.stringPattern("([1-9][0-9]*)|([a-zA-Z\\-][a-zA-Z0-9\\-]*)|([0-9]+[a-zA-Z\\-][a-zA-Z0-9\\-]*)")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val version = SemanticVersion.of(TestVersion(major, minor, patch, preRelease, build).toString(), strict = true)

                // Act
                val versionStr = version.toString()

                // Assert
                SemanticVersion.isValid(versionStr, strict = true) shouldBe true
            }
        }

        test("should print valid strict version strings, even when strict is false") {
            checkAll(
                Arb.int(min = 0),
                Arb.int(min = 0).orNull(),
                Arb.int(min = 0).orNull(),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
                Arb.list(Arb.stringPattern("[a-zA-Z0-9\\-]+")),
            ) { major, minor, patch, preRelease, build ->
                // Arrange
                val version = SemanticVersion.of(TestVersion(major, minor, patch, preRelease, build).toString(), strict = false)

                // Act
                val versionStr = version.toString()

                // Assert
                SemanticVersion.isValid(versionStr, strict = true) shouldBe true
            }
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is true, should print exactly: \"$versionStr\""},
            validTests,
        ) { (expectedVersionStr, version) ->
            // Act
            val versionStr = version.toString()

            // Assert
            versionStr shouldBe expectedVersionStr
        }

        withData(
            nameFn = { (versionStr, _) -> "when strict is false, should print equivalent: \"$versionStr\""},
            validTests + validNonStrictTests
        ) { (_, version) ->
            // Act
            val versionStr = version.toString()

            // Assert
            SemanticVersion.of(versionStr) shouldBe version
        }
    }

})
