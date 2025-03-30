@file:Suppress("DEPRECATION")

package `in`.hridayan.ashell.ui

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import `in`.hridayan.ashell.config.Preferences

object KeyboardUtils {
    /**
     * Interface definition for a callback to be invoked when the keyboard visibility changes.
     */
    interface KeyboardVisibilityListener {
        fun onVisibilityChanged(isVisible: Boolean)
    }

    @JvmStatic
    fun attachVisibilityListener(activity: Activity, listener: KeyboardVisibilityListener) {
        val rootView = activity.window.decorView.rootView

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private var lastState: Boolean = isKeyboardVisible(rootView)

            override fun onGlobalLayout() {
                val isOpen = isKeyboardVisible(rootView)
                if (isOpen != lastState) {
                    lastState = isOpen
                    listener.onVisibilityChanged(isOpen)
                }
            }
        })
    }

    /**
     * Checks if the keyboard is visible.
     *
     * @param rootView The root view of the activity.
     * @return True if the keyboard is visible, false otherwise.
     */
    private fun isKeyboardVisible(rootView: View): Boolean {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val screenHeight = rootView.rootView.height
        val keypadHeight = screenHeight - r.bottom
        return keypadHeight > screenHeight * 0.15
    }

    @JvmStatic
    fun disableKeyboard(activity: Activity, view: View?) {
        val imm =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        val disableSoftKey = Preferences.getDisableSoftkey()
        if (disableSoftKey) {
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)

            activity
                .window
                .setFlags(
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                )
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
    }

    fun showKeyboard(editText: TextInputEditText, context: Context) {
        editText.requestFocus()
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    @JvmStatic
    fun closeKeyboard(activity: Activity, v: View?) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v?.windowToken, 0)
    }
}
