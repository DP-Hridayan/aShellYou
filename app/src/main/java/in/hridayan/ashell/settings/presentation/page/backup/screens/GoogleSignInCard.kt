@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.backup.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun GoogleSignInCard(
    modifier: Modifier = Modifier,
    isSignedIn: Boolean,
    userEmail: String?,
    userName: String? = null,
    userPhotoUrl: Uri? = null,
    isLoading: Boolean = false,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    PillShapedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 5.dp),
        clickable = !isSignedIn && !isLoading,
        onClick = withHaptic { onSignInClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSignedIn) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (isSignedIn) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = if (!isSignedIn) CardDefaults.outlinedCardBorder() else null
    ) {
        AnimatedContent(
            targetState = isSignedIn,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "google_sign_in_state"
        ) { signedIn ->
            if (signedIn) {
                // Signed in state — profile picture + name + email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        photoUrl = userPhotoUrl,
                        displayName = userName ?: userEmail,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AutoResizeableText(
                            text = userName ?: stringResource(R.string.google_drive),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text = userEmail ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    OutlinedButton(
                        onClick = withHaptic(HapticFeedbackType.Reject) { onSignOutClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sign_out),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else {
                // Signed out state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        LoadingIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        AutoResizeableText(
                            text = stringResource(R.string.sign_in_with_google),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AutoResizeableText(
                            text = stringResource(R.string.des_sign_in_with_google),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    modifier: Modifier = Modifier,
    photoUrl: Uri?,
    displayName: String?
) {
    val context = LocalContext.current
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Load profile picture: try disk cache first, then network → save to cache
    LaunchedEffect(photoUrl) {
        profileBitmap = withContext(Dispatchers.IO) {
            val cacheFile = File(context.filesDir, "google_profile.jpg")

            // 1. Try loading from disk cache
            if (cacheFile.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                    if (bitmap != null) return@withContext bitmap.asImageBitmap()
                } catch (_: Exception) {
                }
            }

            // 2. Fall back to network
            if (photoUrl != null) {
                try {
                    val stream = URL(photoUrl.toString()).openStream()
                    val bytes = stream.readBytes()
                    stream.close()

                    // Save to disk cache
                    cacheFile.writeBytes(bytes)

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap!!,
                contentDescription = null,
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback: colored circle with initial
            val initial = displayName?.firstOrNull()?.uppercaseChar() ?: '?'
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
