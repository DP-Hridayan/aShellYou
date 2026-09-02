package `in`.hridayan.ashell.settings.presentation.page.privacypolicy.util

import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.model.PolicyBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyParserTest {

    @Test
    fun testHeadingParsing() {
        val markdown = """
            # Heading 1
            ## Heading 2
            ### Heading 3
            #### Heading 4
        """.trimIndent()

        val blocks = parsePolicy(markdown)
        assertEquals(4, blocks.size)

        assertEquals(1, (blocks[0] as PolicyBlock.Heading).level)
        assertEquals("Heading 1", (blocks[0] as PolicyBlock.Heading).text)

        assertEquals(2, (blocks[1] as PolicyBlock.Heading).level)
        assertEquals("Heading 2", (blocks[1] as PolicyBlock.Heading).text)
    }

    @Test
    fun testParagraphAndHardWrapping() {
        val markdown = """
            This is a paragraph
            that spans multiple lines
            due to hard wrapping.
            
            This is a second paragraph.
        """.trimIndent()

        val blocks = parsePolicy(markdown)
        assertEquals(3, blocks.size) // Para, BlankLine, Para

        assertTrue(blocks[0] is PolicyBlock.Paragraph)
        assertEquals(
            "This is a paragraph that spans multiple lines due to hard wrapping.",
            (blocks[0] as PolicyBlock.Paragraph).text
        )

        assertTrue(blocks[1] is PolicyBlock.BlankLine)

        assertTrue(blocks[2] is PolicyBlock.Paragraph)
        assertEquals("This is a second paragraph.", (blocks[2] as PolicyBlock.Paragraph).text)
    }

    @Test
    fun testBulletItemsAndHardWrapping() {
        val markdown = """
            - First bullet
              is wrapped to second line.
            - Second bullet
            * Third bullet with asterisk
        """.trimIndent()

        val blocks = parsePolicy(markdown)
        assertEquals(3, blocks.size)

        assertTrue(blocks[0] is PolicyBlock.BulletItem)
        assertEquals(
            "First bullet is wrapped to second line.",
            (blocks[0] as PolicyBlock.BulletItem).text
        )
        assertEquals(0, (blocks[0] as PolicyBlock.BulletItem).depth)

        assertTrue(blocks[1] is PolicyBlock.BulletItem)
        assertEquals("Second bullet", (blocks[1] as PolicyBlock.BulletItem).text)

        assertTrue(blocks[2] is PolicyBlock.BulletItem)
        assertEquals("Third bullet with asterisk", (blocks[2] as PolicyBlock.BulletItem).text)
    }

    @Test
    fun testBlockquoteAndConsecutiveMerging() {
        val markdown = """
            > This is a quote.
            > It continues on the next line.
            And it wraps to a third line without the prefix.
        """.trimIndent()

        val blocks = parsePolicy(markdown)
        assertEquals(1, blocks.size)

        assertTrue(blocks[0] is PolicyBlock.BlockQuote)
        assertEquals(
            "This is a quote. It continues on the next line. And it wraps to a third line without the prefix.",
            (blocks[0] as PolicyBlock.BlockQuote).text
        )
    }
}
