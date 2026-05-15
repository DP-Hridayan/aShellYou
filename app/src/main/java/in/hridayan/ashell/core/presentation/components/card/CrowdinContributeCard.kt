package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.utils.UrlUtils

@Composable
fun CrowdinContributeCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_translate),
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.want_to_contribute_too),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                )

                Text(
                    text = stringResource(R.string.crowdin_contribution_msg),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )

                IconWithTextButton(
                    icon = painterResource(R.drawable.ic_crowdin),
                    text = stringResource(R.string.crowdin),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    onClick = withHaptic {
                        UrlUtils.openUrl(
                            context = context,
                            url = UrlConst.URL_CROWDIN_PROJECT
                        )
                    })
            }
        }
    }
}
