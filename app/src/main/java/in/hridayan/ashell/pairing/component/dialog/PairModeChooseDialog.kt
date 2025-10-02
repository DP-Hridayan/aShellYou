@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.pairing.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.BottomCornerRoundedCard
import `in`.hridayan.ashell.core.presentation.components.card.TopCornerRoundedCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun PairModeChooseDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onClickPairSelf: () -> Unit = {},
    onClickPairAnother: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current

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
                    text = stringResource(R.string.which_device_to_pair),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                TopCornerRoundedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    onClick = {
                        weakHaptic()
                        onClickPairSelf()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mobile),
                            contentDescription = null,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            AutoResizeableText(
                                text = stringResource(R.string.pair_this_device),
                                style = MaterialTheme.typography.bodyLargeEmphasized
                            )
                            Text(
                                text = stringResource(R.string.self),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                BottomCornerRoundedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        weakHaptic()
                        onClickPairAnother()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mobile_loupe),
                            contentDescription = null,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            AutoResizeableText(
                                text = stringResource(R.string.pair_another_device),
                                style = MaterialTheme.typography.bodyLargeEmphasized
                            )
                            Text(
                                text = stringResource(R.string.des_pair_another_device),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}