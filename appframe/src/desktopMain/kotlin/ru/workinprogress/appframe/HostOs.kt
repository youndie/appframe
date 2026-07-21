package ru.workinprogress.appframe

/**
 * The desktop operating system the application is running on.
 *
 * Used to pick platform-appropriate window decorations: window controls sit on the left on macOS
 * and on the right on Windows and Linux.
 */
public enum class HostOs {
    Windows,
    MacOs,
    Linux,
    Unknown,
    ;

    public companion object {
        /** The OS this JVM is running on, resolved once. */
        public val current: HostOs by lazy { hostOsOf(System.getProperty("os.name")) }
    }
}

internal fun hostOsOf(osName: String?): HostOs {
    val name = osName?.lowercase().orEmpty()
    return when {
        name.startsWith("win") -> HostOs.Windows

        name.startsWith("mac") || name.contains("darwin") -> HostOs.MacOs

        name.contains("linux") || name.contains("nix") || name.contains("nux") ||
            name.contains("bsd") || name.contains("sunos") || name.contains("aix") -> HostOs.Linux

        else -> HostOs.Unknown
    }
}
