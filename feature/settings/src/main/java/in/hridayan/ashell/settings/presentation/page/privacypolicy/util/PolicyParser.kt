package `in`.hridayan.ashell.settings.presentation.page.privacypolicy.util

import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.model.PolicyBlock

/**
 * Parses the privacy policy Markdown into a list of [PolicyBlock] items.
 * Handles: headings (H1-H4), paragraphs, bullet lists, tables, block-quotes,
 * horizontal rules, blank lines, and strips raw HTML tags (e.g. <br>).
 */
fun parsePolicy(text: String): List<PolicyBlock> {
    // Replace <br> variants with a special placeholder to preserve actual newlines within blocks
    val brPlaceholder = "\u2028"
    val cleaned = text
        .replace(Regex("<\\/?br\\s*\\/?>", RegexOption.IGNORE_CASE), brPlaceholder)
        .replace(Regex("<[^>]+>"), "")

    val lines = cleaned.lines()
    val blocks = mutableListOf<PolicyBlock>()
    var i = 0

    while (i < lines.size) {
        val raw = lines[i]
        val trimmed = raw.trim()

        when {
            // Blank line – collapse consecutive blanks to one
            trimmed.isEmpty() -> {
                if (blocks.lastOrNull() !is PolicyBlock.BlankLine) {
                    blocks.add(PolicyBlock.BlankLine)
                }
                i++
            }

            // Horizontal rule
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                blocks.add(PolicyBlock.HorizontalRule)
                i++
            }

            // Headings
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(4)
                blocks.add(PolicyBlock.Heading(level, trimmed.drop(level).trim()))
                i++
            }

            // Bullet / unordered list (- or *)
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val depth = (raw.length - raw.trimStart().length) / 2
                blocks.add(PolicyBlock.BulletItem(trimmed.drop(2).trim(), depth))
                i++
            }

            // Table (pipe-delimited)
            trimmed.startsWith("|") -> {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                val isSeparator = { cells: List<String> ->
                    cells.all { it.all { c -> c == '-' || c == ':' || c == ' ' || c == '|' } }
                }
                val parsed = tableLines
                    .map { line ->
                        line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    .filter { it.isNotEmpty() && !isSeparator(it) }

                if (parsed.isNotEmpty()) {
                    blocks.add(PolicyBlock.TableData(parsed.first(), parsed.drop(1)))
                }
            }

            // Block-quote
            trimmed.startsWith("> ") -> {
                val last = blocks.lastOrNull()
                if (last is PolicyBlock.BlockQuote) {
                    blocks[blocks.lastIndex] =
                        last.copy(text = last.text + " " + trimmed.drop(2).trim())
                } else {
                    blocks.add(PolicyBlock.BlockQuote(trimmed.drop(2).trim()))
                }
                i++
            }

            // Regular text (Paragraph or continuation of previous block)
            else -> {
                val last = blocks.lastOrNull()
                if (last is PolicyBlock.Paragraph) {
                    blocks[blocks.lastIndex] = last.copy(text = last.text + " " + trimmed)
                } else if (last is PolicyBlock.BulletItem) {
                    blocks[blocks.lastIndex] = last.copy(text = last.text + " " + trimmed)
                } else if (last is PolicyBlock.BlockQuote) {
                    blocks[blocks.lastIndex] = last.copy(text = last.text + " " + trimmed)
                } else {
                    blocks.add(PolicyBlock.Paragraph(trimmed))
                }
                i++
            }
        }
    }

    return blocks.map { block ->
        when (block) {
            is PolicyBlock.Heading -> block.copy(
                text = block.text.replace(
                    Regex("$brPlaceholder\\s*"),
                    "\n"
                )
            )

            is PolicyBlock.Paragraph -> block.copy(
                text = block.text.replace(
                    Regex("$brPlaceholder\\s*"),
                    "\n"
                )
            )

            is PolicyBlock.BulletItem -> block.copy(
                text = block.text.replace(
                    Regex("$brPlaceholder\\s*"),
                    "\n"
                )
            )

            is PolicyBlock.BlockQuote -> block.copy(
                text = block.text.replace(
                    Regex("$brPlaceholder\\s*"),
                    "\n"
                )
            )

            is PolicyBlock.TableData -> block
            PolicyBlock.HorizontalRule -> block
            PolicyBlock.BlankLine -> block
        }
    }
}