package `in`.hridayan.ashell.settings.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.common.domain.model.LogcatBufferSize
import `in`.hridayan.ashell.core.resources.R

/** Label for a logcat buffer budget, shown in the settings row and in the chooser dialog. */
@Composable
fun logcatBufferSizeLabel(megabytes: Int): String =
    stringResource(R.string.size_megabytes, LogcatBufferSize.sanitize(megabytes))
