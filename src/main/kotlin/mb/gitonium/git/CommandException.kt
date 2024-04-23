package mb.gitonium.git

import java.io.IOException

/** An exception that is thrown when a command fails. */
class CommandException(
    /** The command that failed. */
    val command: String,
    /** The exit code of the command. */
    val exitCode: Int,
    /** The standard error output of the command. */
    val stderr: String,
    /** The error message, or null to use a default message. */
    message: String? = null,
): IOException(
    (message ?: "Command failed with exit code $exitCode") + if(stderr.isNotBlank()) ": $stderr" else "."
)
