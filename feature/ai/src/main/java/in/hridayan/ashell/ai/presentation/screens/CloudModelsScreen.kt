@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.ai.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.ai.presentation.viewmodel.CloudModelsViewModel
import `in`.hridayan.ashell.ai.presentation.viewmodel.VerificationState
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
import `in`.hridayan.ashell.core.resources.R

@Composable
fun CloudModelsScreen(viewModel: CloudModelsViewModel = hiltViewModel()) {
    val navController = LocalNavController.current

    val verificationState by viewModel.verificationState.collectAsState()

    val listState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()

    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBarTitle = stringResource(R.string.cloud_models),
        listState = listState,
        topAppBarState = topBarState,
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 15.dp),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    AutoResizeableText(
                        modifier = Modifier.padding(bottom = 10.dp, start = 5.dp, end = 5.dp),
                        text = stringResource(R.string.api_key),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Since we only have Gemini now, we just hardcode it for simplicity, but we can iterate LlmProvider.all
                    LlmProvider.all.forEach { provider ->

                        val hasKey by viewModel.hasKey(provider).collectAsState(initial = false)

                        ApiKeySection(
                            modifier = Modifier.fillMaxWidth(),
                            provider = provider,
                            hasKey = hasKey,
                            onDeleteApiKey = { viewModel.deleteApiKey(provider) },
                            onSaveApiKey = { key -> viewModel.saveApiKey(provider, key) },
                            onVerifyApiKey = { viewModel.verifyKey(provider) },
                            verificationState = verificationState
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun ApiKeySection(
    modifier: Modifier = Modifier,
    provider: LlmProvider,
    hasKey: Boolean,
    onDeleteApiKey: () -> Unit,
    onVerifyApiKey: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    verificationState: VerificationState
) {
    var isExpanded by rememberSaveable(provider) { mutableStateOf(false) }

    CustomCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            ApiKeyHeader(
                provider = provider,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )

            Spacer(Modifier.height(10.dp))

            if (hasKey) {
                SavedKeySection(
                    onDeleteApiKey = onDeleteApiKey,
                    onVerifyApiKey = onVerifyApiKey,
                    verificationState = verificationState
                )
            } else {
                InputKeySection(
                    provider = provider,
                    onSaveApiKey = onSaveApiKey,
                    onVerifyApiKey = onVerifyApiKey
                )
            }

            if (isExpanded) {
                if (provider == LlmProvider.Gemini) {
                    GeminiHelpSection()
                }
            }
        }
    }
}

@Composable
private fun ApiKeyHeader(
    provider: LlmProvider,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = provider.displayName,
            style = MaterialTheme.typography.titleMediumEmphasized,
            fontWeight = FontWeight.SemiBold
        )

        if (provider == LlmProvider.Gemini) {
            TextButton(
                onClick = withHaptic { onToggleExpand() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = stringResource(R.string.get_api_key))

                Spacer(Modifier.width(4.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SavedKeySection(
    onDeleteApiKey: () -> Unit,
    onVerifyApiKey: () -> Unit,
    verificationState: VerificationState
) {
    val isVerifying = verificationState is VerificationState.Loading

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.api_key_is_saved),
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = withHaptic { onDeleteApiKey() }) {
            Text(text = stringResource(R.string.remove))
        }
    }

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = withHaptic { onVerifyApiKey() },
        enabled = !isVerifying,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isVerifying) {
            LoadingIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))

        Text(
            text = if (isVerifying) {
                stringResource(R.string.verifying)
            } else {
                stringResource(R.string.verify_api_key)
            }
        )
    }

    val displayData = when (verificationState) {
        is VerificationState.Success -> Triple(
            R.drawable.ic_verified,
            MaterialTheme.colorScheme.primary,
            verificationState.message
        )

        is VerificationState.Error -> Triple(
            R.drawable.ic_error,
            MaterialTheme.colorScheme.error,
            verificationState.message
        )

        else -> null
    }

    displayData?.let { (icon, color, message) ->
        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(icon),
                tint = color,
                contentDescription = null
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.InputKeySection(
    provider: LlmProvider,
    onSaveApiKey: (String) -> Unit,
    onVerifyApiKey: () -> Unit
) {
    var keyInput by remember(provider) { mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = keyInput,
        onValueChange = { keyInput = it },
        label = { Text(stringResource(R.string.api_key)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
    )

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = withHaptic {
            onSaveApiKey(keyInput)
            onVerifyApiKey()
            keyInput = ""
        },
        enabled = keyInput.isNotBlank(),
        modifier = Modifier.align(Alignment.End),
    ) {
        Text(text = stringResource(R.string.save_key))
    }
}

@Composable
private fun GeminiHelpSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(16.dp))

        val primaryColor = MaterialTheme.colorScheme.primary

        val urlText = UrlConst.URL_GOOGLE_AI_STUDIO

        val step1String = stringResource(R.string.gemini_api_key_step_1, urlText)
        val step2String = stringResource(R.string.gemini_api_key_step_2)
        val step3String = stringResource(R.string.gemini_api_key_step_3)
        val step4String = stringResource(R.string.gemini_api_key_step_4)

        val startIndex = step1String.indexOf(urlText)

        val step1Annotated = remember(step1String, primaryColor) {
            buildAnnotatedString {
                append(step1String)
                val start = if (startIndex >= 0) startIndex else 0
                val end =
                    if (startIndex >= 0) startIndex + urlText.length else step1String.length
                addLink(
                    url = LinkAnnotation.Url(UrlConst.URL_GOOGLE_GEMINI_API_KEY),
                    start = start,
                    end = end
                )
                addStyle(
                    style = SpanStyle(
                        color = primaryColor,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )
            }
        }

        val steps =
            remember(step1Annotated, step2String, step3String, step4String) {
                listOf(
                    step1Annotated,
                    AnnotatedString(step2String),
                    AnnotatedString(step3String),
                    AnnotatedString(step4String)
                )
            }

        BulletPointsTextLayout(
            modifier = Modifier.fillMaxWidth(),
            annotatedTextLines = steps,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        )
    }
}
