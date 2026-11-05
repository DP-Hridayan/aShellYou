package `in`.hridayan.ashell.shell.common.data.parser

import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import javax.inject.Inject
import javax.inject.Singleton

private const val PACKAGE_PREFIX = "package:"
private const val ASHELL_DELIMITER = "---ASHELL_DELIMITER---"
private const val SPACE = " "
private const val FETCH_COMMAND = "pm list packages -s; echo \"$ASHELL_DELIMITER\"; pm list packages -3"

@Singleton
class RemotePackageParser @Inject constructor() {

    val delimiter: String = ASHELL_DELIMITER
    val fetchCommand: String = FETCH_COMMAND

    fun parse(lines: List<String>): List<PackageInfo> {
        val sanitizedLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (sanitizedLines.isEmpty()) return emptyList()

        val delimiterIndex = sanitizedLines.indexOf(ASHELL_DELIMITER)
        return if (delimiterIndex != -1) {
            parseDelimited(sanitizedLines, delimiterIndex)
        } else {
            parseWithoutDelimiter(sanitizedLines)
        }
    }

    private fun parseDelimited(lines: List<String>, delimiterIndex: Int): List<PackageInfo> {
        val systemLines = lines.subList(0, delimiterIndex)
        val userLines = lines.subList(delimiterIndex + 1, lines.size)

        val systemPackages = extractPackages(systemLines, isSystem = true)
        val userPackages = extractPackages(userLines, isSystem = false)

        return (systemPackages + userPackages)
            .distinctBy { it.packageName }
            .sortedBy { it.packageName }
    }

    private fun parseWithoutDelimiter(lines: List<String>): List<PackageInfo> {
        return extractPackages(lines, isSystem = false)
            .distinctBy { it.packageName }
            .sortedBy { it.packageName }
    }

    private fun extractPackages(lines: List<String>, isSystem: Boolean): List<PackageInfo> {
        return lines.mapNotNull { line ->
            extractPackageName(line)?.let { name ->
                PackageInfo(packageName = name, isSystemApp = isSystem)
            }
        }
    }

    private fun extractPackageName(line: String): String? {
        if (!line.startsWith(PACKAGE_PREFIX)) return null
        val rawPackage = line.removePrefix(PACKAGE_PREFIX).trim()
        val packageName = rawPackage.substringBefore(SPACE).trim()
        return packageName.ifEmpty { null }
    }
}
