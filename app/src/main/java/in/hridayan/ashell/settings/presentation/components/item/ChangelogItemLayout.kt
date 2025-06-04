package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun ChangelogItemLayout(
    modifier: Modifier = Modifier,
    versionName: String,
    changelog: List<String>,
) {
    var isLatestVersion = versionName == BuildConfig.VERSION_NAME
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        AutoResizeableText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.version) + "\t\t$versionName",
            style = if (isLatestVersion) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
            color = if (isLatestVersion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        changelog.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alignBy { it.measuredHeight }
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignBy(FirstBaseline)
                )
            }
        }
    }
}