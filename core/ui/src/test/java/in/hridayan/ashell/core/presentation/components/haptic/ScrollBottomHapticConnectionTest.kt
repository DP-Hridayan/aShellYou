package `in`.hridayan.ashell.core.presentation.components.haptic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ScrollBottomHapticConnection] verifying boundary detection,
 * latching, debounce behavior, and gesture isolation.
 */
class ScrollBottomHapticConnectionTest {

    private class HapticTriggerRecorder {
        var triggerCount = 0

        fun onTrigger() {
            triggerCount++
        }
    }

    @Test
    fun onPostScroll_whenDownwardScrollHitsBottom_triggersHapticOnce() {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { true },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -20f),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput
        )

        connection.onPostScroll(
            consumed = Offset(0f, -5f),
            available = Offset(0f, -15f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(1, recorder.triggerCount)
    }

    @Test
    fun onPostScroll_whenContinuousDragAtBottom_doesNotTriggerRepeatedly() {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { true },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -20f),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput
        )

        connection.onPostScroll(
            consumed = Offset(0f, -5f),
            available = Offset(0f, -10f),
            source = NestedScrollSource.UserInput
        )

        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(0f, -10f),
            source = NestedScrollSource.UserInput
        )

        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(0f, -15f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(1, recorder.triggerCount)
    }

    @Test
    fun onPostScroll_whenReverseScrollFollowedByBottomHit_triggersAgain() {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { true },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -20f),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput
        )
        connection.onPostScroll(
            consumed = Offset(0f, -5f),
            available = Offset(0f, -10f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(1, recorder.triggerCount)

        connection.onPreScroll(
            available = Offset(0f, 25f),
            source = NestedScrollSource.UserInput
        )

        connection.onPostScroll(
            consumed = Offset(0f, -10f),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput
        )
        connection.onPostScroll(
            consumed = Offset(0f, -2f),
            available = Offset(0f, -8f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(2, recorder.triggerCount)
    }

    @Test
    fun onPostScroll_whenDisabled_doesNotTrigger() {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { false },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -20f),
            available = Offset(0f, -10f),
            source = NestedScrollSource.UserInput
        )

        assertEquals(0, recorder.triggerCount)
    }

    @Test
    fun onPostScroll_whenProgrammaticScroll_doesNotTrigger() {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { true },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -20f),
            available = Offset(0f, -10f),
            source = NestedScrollSource.SideEffect
        )

        assertEquals(0, recorder.triggerCount)
    }

    @Test
    fun onPostFling_whenFlungToBottom_triggersHaptic() = runBlocking {
        val recorder = HapticTriggerRecorder()
        val connection = ScrollBottomHapticConnection(
            isEnabled = { true },
            onBottomReached = recorder::onTrigger
        )

        connection.onPostScroll(
            consumed = Offset(0f, -50f),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput
        )

        connection.onPostFling(
            consumed = Velocity(0f, -500f),
            available = Velocity(0f, -200f)
        )

        assertEquals(1, recorder.triggerCount)
    }
}
