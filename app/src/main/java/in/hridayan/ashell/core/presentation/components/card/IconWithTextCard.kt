@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.Dimens

@Composable
fun IconWithTextCard(
    text: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardDefaults.shape(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ),
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    CustomCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        elevation = elevation,
        colors = colors
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Dimens.paddingLarge)
                    .size(Dimens.iconSizeLarge)
            )
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmallEmphasized,
                )
                content()
            }
        }
    }
}