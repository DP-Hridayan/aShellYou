@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.settings.presentation.provider.ButtonGroupOptionsProvider

@Composable
fun CreateNewTileScreen(modifier: Modifier = Modifier) {
    val weakHaptic = LocalWeakHaptic.current

    var tileName by rememberSaveable { mutableStateOf("") }
    val tileNameHint = "Reboot"

    var adbCommand by rememberSaveable { mutableStateOf("") }
    val adbCommandHint = "adb reboot"

    val executionMethodOptions = ButtonGroupOptionsProvider.tileServiceAdbExecutionMethod
    var selectedExecutionMethod by rememberSaveable { mutableStateOf(executionMethodOptions[0]) }
    val onSelectedMethodChange: (Int) -> Unit = {
        selectedExecutionMethod = executionMethodOptions[it]
    }

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = paddingValues
        ) {
            item {
                AutoResizeableText(
                    text = stringResource(R.string.tile_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        top = 25.dp,
                        start = 25.dp,
                        bottom = 10.dp
                    )
                )
            }

            item {
                PillShapedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        value = tileName.ifEmpty { tileNameHint },
                        onValueChange = { tileName = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (tileName.isEmpty()) 0.6f else 1f)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            item {
                AutoResizeableText(
                    text = stringResource(R.string.adb_command),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        top = 25.dp,
                        start = 25.dp,
                        bottom = 10.dp
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        value = adbCommand.ifEmpty { adbCommandHint },
                        onValueChange = { adbCommand = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (adbCommand.isEmpty()) 0.6f else 1f)

                        ),
                        minLines = 3,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            item {
                AutoResizeableText(
                    text = stringResource(R.string.execution_method),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        top = 25.dp,
                        start = 25.dp,
                        bottom = 10.dp
                    )
                )
            }

            item {
                Row(
                    modifier = modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    executionMethodOptions.forEachIndexed { index, option ->
                        ToggleButton(
                            checked = option.value == selectedExecutionMethod.value,
                            onCheckedChange = {
                                onSelectedMethodChange(option.value)
                                weakHaptic()
                            },
                            modifier = Modifier.weight(1f),
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                executionMethodOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            option.labelResId?.let {
                                Text(stringResource(it))
                            }
                        }
                    }
                }
            }
            
            item {
                Button(
                    modifier = Modifier
                        .heightIn(70.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 25.dp),
                    onClick = {

                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    AutoResizeableText(
                        text = stringResource(R.string.generate_tile),
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_help),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewScreen() {
    CompositionLocalProvider(
        LocalWeakHaptic provides {}
    ) {
        CreateNewTileScreen()
    }
}