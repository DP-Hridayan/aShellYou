package `in`.hridayan.ashell.guides.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import `in`.hridayan.ashell.guides.domain.model.GuideCategory

@Composable
fun GuideCategorySectionHeader(
    category: GuideCategory,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(category.titleRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
