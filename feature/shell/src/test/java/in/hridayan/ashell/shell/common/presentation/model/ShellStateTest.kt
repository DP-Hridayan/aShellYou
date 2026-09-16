package `in`.hridayan.ashell.shell.common.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellStateTest {

    @Test
    fun `empty input is free`() {
        assertEquals(ShellState.Free, ShellState.forInput(""))
    }

    @Test
    fun `whitespace only input is free`() {
        assertEquals(ShellState.Free, ShellState.forInput("   "))
    }

    @Test
    fun `text input becomes a query carrying that text`() {
        assertEquals(ShellState.InputQuery("ls -la"), ShellState.forInput("ls -la"))
    }
}
