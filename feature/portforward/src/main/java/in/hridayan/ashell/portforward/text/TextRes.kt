package `in`.hridayan.ashell.portforward.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** A string resource and its format arguments, resolved where a [Context] is at hand. */
data class TextRes(@param:StringRes val id: Int, val args: List<Any> = emptyList())

fun Context.resolve(text: TextRes): String = getString(text.id, *text.args.toTypedArray())

@Composable
fun TextRes.resolve(): String = stringResource(id, *args.toTypedArray())
