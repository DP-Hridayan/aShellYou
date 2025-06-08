package `in`.hridayan.ashell.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.delay

/**
 * Detects whether the keyboard is visible on screen in Compose.
 */
@Composable
fun isKeyboardVisible(): State<Boolean> {
    val rootView = LocalView.current
    val keyboardVisible = remember { mutableStateOf(false) }

    DisposableEffect(rootView) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            keyboardVisible.value = keypadHeight > screenHeight * 0.15
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return keyboardVisible
}

/**
 * Hides the soft keyboard from the screen.
 */
fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val view = (context as? Activity)?.currentFocus ?: View(context)
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Shows the soft keyboard on screen manually.
 */
fun showKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

/**
 * A ready-to-use TextField that auto-focuses and shows the keyboard.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FocusableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // Auto focus and show keyboard
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        showKeyboard(context)
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = androidx.compose.ui.Modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) showKeyboard(context)
            },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeAction()
                hideKeyboard(context)
            }
        )
    )
}
