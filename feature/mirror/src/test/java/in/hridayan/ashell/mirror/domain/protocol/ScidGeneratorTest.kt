package `in`.hridayan.ashell.mirror.domain.protocol

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ScidGeneratorTest {

    @Test
    fun `scids are non negative 31 bit values`() {
        val generator = ScidGenerator(Random(seed = 1))

        repeat(1_000) {
            val scid = generator.next()
            assertTrue(scid in 0..Int.MAX_VALUE)
        }
    }
}
