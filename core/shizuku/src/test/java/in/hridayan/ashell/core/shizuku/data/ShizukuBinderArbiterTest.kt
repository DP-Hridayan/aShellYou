package `in`.hridayan.ashell.core.shizuku.data

import `in`.hridayan.ashell.core.shizuku.domain.ShizukuBinderSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShizukuBinderArbiterTest {

    @Before
    fun reset() {
        ShizukuBinderArbiter.onBinderLost()
    }

    @Test
    fun `stock binder is always accepted`() {
        ShizukuBinderArbiter.onAccepted(ShizukuBinderSource.PLUS)

        assertTrue(ShizukuBinderArbiter.shouldAccept(ShizukuBinderSource.STOCK, binderAlive = true))
    }

    @Test
    fun `plus binder is ignored while a live stock binder is held`() {
        ShizukuBinderArbiter.onAccepted(ShizukuBinderSource.STOCK)

        assertFalse(ShizukuBinderArbiter.shouldAccept(ShizukuBinderSource.PLUS, binderAlive = true))
    }

    @Test
    fun `plus binder is accepted once the stock binder is dead`() {
        ShizukuBinderArbiter.onAccepted(ShizukuBinderSource.STOCK)

        assertTrue(ShizukuBinderArbiter.shouldAccept(ShizukuBinderSource.PLUS, binderAlive = false))
    }

    @Test
    fun `plus binder is accepted when nothing is held`() {
        assertTrue(ShizukuBinderArbiter.shouldAccept(ShizukuBinderSource.PLUS, binderAlive = false))
    }
}
