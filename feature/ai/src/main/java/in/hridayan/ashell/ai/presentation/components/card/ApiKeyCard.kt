@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.ai.presentation.components.card

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.presentation.model.ApiKeyHelp
import `in`.hridayan.ashell.ai.presentation.model.apiKeyHelpFor
import `in`.hridayan.ashell.ai.presentation.viewmodel.VerificationState
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
import `in`.hridayan.ashell.core.resources.R

/**
 * One provider's API key controls: entry, verification, removal and a link to obtain a key.
 *
 * Every provider gets the same affordances, so adding a provider needs no change here beyond its
 * help content.
 */
@Composable
fun ApiKeyCard(
    modifier: Modifier = Modifier,
    provider: LlmProvider,
    hasKey: Boolean,
    verificationState: VerificationState,
    onSaveApiKey: (String) -> Unit,
    onDeleteApiKey: () -> Unit,
    onVerifyApiKey: () -> Unit,
) {
    var isExpanded by rememberSaveable(provider.id) { mutableStateOf(false) }
    val help = apiKeyHelpFor(provider)

    CustomCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            ApiKeyHeader(
                title = provider.displayName,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )

            Spacer(Modifier.height(10.dp))

            if (hasKey) {
                SavedKeySection(
                    verificationState = verificationState,
                    onDeleteApiKey = onDeleteApiKey,
                    onVerifyApiKey = onVerifyApiKey,
                )
            } else {
                InputKeySection(
                    provider = provider,
                    verificationState = verificationState,
                    onSaveApiKey = onSaveApiKey,
                )
            }

            if (isExpanded) ApiKeyHelpSection(help = help)
        }
    }
}

@Composable
private fun ApiKeyHeader(
    title: String,
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
            text = title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            fontWeight = FontWeight.SemiBold
        )

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

@Composable
private fun SavedKeySection(
    verificationState: VerificationState,
    onDeleteApiKey: () -> Unit,
    onVerifyApiKey: () -> Unit,
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
            LoadingIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }

        Text(
            text = stringResource(
                if (isVerifying) R.string.verifying else R.string.verify_api_key
            )
        )
    }

    VerificationMessage(verificationState = verificationState)
}

@Composable
private fun VerificationMessage(verificationState: VerificationState) {
    val icon: Int
    val color: Color
    val message: String

    when (verificationState) {
        is VerificationState.Success -> {
            icon = R.drawable.ic_verified
            color = MaterialTheme.colorScheme.primary
            message = verificationState.message
        }

        is VerificationState.Error -> {
            icon = R.drawable.ic_error
            color = MaterialTheme.colorScheme.error
            message = verificationState.message
        }

        else -> return
    }

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

        Text(text = message, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun ColumnScope.InputKeySection(
    provider: LlmProvider,
    verificationState: VerificationState,
    onSaveApiKey: (String) -> Unit,
) {
    var keyInput by remember(provider.id) { mutableStateOf("") }
    val isVerifying = verificationState is VerificationState.Loading

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = keyInput,
        onValueChange = { keyInput = it },
        label = { Text(stringResource(R.string.api_key)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        enabled = !isVerifying,
        isError = verificationState is VerificationState.Error,
    )

    Spacer(Modifier.height(8.dp))

    // The key is left in the field on failure so a rejected key can be corrected rather than retyped.
    Button(
        onClick = withHaptic { onSaveApiKey(keyInput) },
        enabled = keyInput.isNotBlank() && !isVerifying,
        modifier = Modifier.align(Alignment.End),
    ) {
        if (isVerifying) {
            LoadingIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }

        Text(
            text = stringResource(
                if (isVerifying) R.string.verifying else R.string.save_key
            )
        )
    }

    VerificationMessage(verificationState = verificationState)
}

@Composable
private fun ApiKeyHelpSection(help: ApiKeyHelp) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Spacer(Modifier.height(16.dp))

        val primaryColor = MaterialTheme.colorScheme.primary
        val firstStep = stringResource(help.firstStepRes, help.displayUrl)
        val remainingSteps = help.remainingStepRes.map { stringResource(it) }

        val steps = remember(firstStep, remainingSteps, primaryColor) {
            buildList {
                add(linkify(firstStep, help.displayUrl, help.url, primaryColor))
                addAll(remainingSteps.map(::AnnotatedString))
            }
        }

        BulletPointsTextLayout(
            modifier = Modifier.fillMaxWidth(),
            annotatedTextLines = steps,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        )
    }
}

private fun linkify(
    text: String,
    displayUrl: String,
    url: String,
    color: Color,
): AnnotatedString = buildAnnotatedString {
    append(text)

    val startIndex = text.indexOf(displayUrl)
    val start = if (startIndex >= 0) startIndex else 0
    val end = if (startIndex >= 0) startIndex + displayUrl.length else text.length

    addLink(url = LinkAnnotation.Url(url), start = start, end = end)
    addStyle(
        style = SpanStyle(color = color, textDecoration = TextDecoration.Underline),
        start = start,
        end = end
    )
}
