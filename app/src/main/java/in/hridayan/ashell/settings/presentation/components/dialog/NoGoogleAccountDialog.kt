@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape

@Composable
fun NoGoogleAccountDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onAddAccount: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_google_account_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.no_google_account_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = withHaptic {
                        onAddAccount()
                        onDismiss()
                    },
                    shape = CustomCardShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        AutoResizeableText(
                            text = stringResource(R.string.add_account),
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CustomCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = withHaptic {
                        onDismiss()
                    },
                    shape = CustomCardShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp)
                    )
                }
            }
        }
    }
}
