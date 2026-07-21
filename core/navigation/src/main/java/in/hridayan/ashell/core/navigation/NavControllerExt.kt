package `in`.hridayan.ashell.core.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

/**
 * Safely pops the back stack only if the current lifecycle is at least RESUMED.
 *
 * This prevents crashes caused by:
 * 1. Multiple rapid back presses (double-pop on an empty or transitioning stack).
 * 2. Back gesture race conditions on MIUI / predictive-back enabled devices, where the
 *    system-level TransitionRecord (TO_BACK) can conflict with a Compose NavHost pop
 *    transition that is still in flight, resulting in a TransitionChain mismatch and
 *    a force-close.
 */
fun NavController.navigateBack() {
    val currentEntry = currentBackStackEntry ?: return
    if (currentEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}
