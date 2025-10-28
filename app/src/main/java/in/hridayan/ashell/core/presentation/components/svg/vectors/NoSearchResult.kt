package `in`.hridayan.ashell.core.presentation.components.svg.vectors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors

@Composable
fun DynamicColorImageVectors.noSearchResult(): ImageVector {
    return Builder(
        name = "noSearchResult",
        defaultWidth = 480.dp,
        defaultHeight = 360.dp,
        viewportWidth = 480f,
        viewportHeight = 360f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M430 170c0 85 -75 140 -190 140S50 250 50 170 130 40 240 40s190 45 190 130z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceContainer),
            fillAlpha = 0.75f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M164 100H336A14 14 0 0 1 350 114V206A14 14 0 0 1 336 220H164A14 14 0 0 1 150 206V114A14 14 0 0 1 164 100Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceContainer),
            stroke = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
            strokeLineWidth = 3f,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineCap = StrokeCap.Round
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M182 125H263A7 7 0 0 1 270 132V132A7 7 0 0 1 263 139H182A7 7 0 0 1 175 132V132A7 7 0 0 1 182 125Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primaryContainer)
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M180 149H240A5 5 0 0 1 245 154V154A5 5 0 0 1 240 159H180A5 5 0 0 1 175 154V154A5 5 0 0 1 180 149Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiaryContainer),
            fillAlpha = 0.75f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M322 210A22 22 0 0 1 278 210A22 22 0 0 1 322 210Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceContainer),
            fillAlpha = 0.75f,
            stroke = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
            strokeLineWidth = 3f,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineCap = StrokeCap.Round
        )
        group(
            rotate = 45f,
            pivotX = 318f,
            pivotY = 228f
        ) {
            addPath(
                fill = SolidColor(MaterialTheme.colorScheme.primary),
                pathData = PathParser().parsePathString(
                    "M322 228H336A4 4 0 0 1 340 232V232A4 4 0 0 1 336 236H322A4 4 0 0 1 318 232V232A4 4 0 0 1 322 228Z"
                ).toNodes()
            )
        }
        addPath(
            pathData = PathParser().parsePathString(
                "M350 330A110 18 0 0 1 130 330A110 18 0 0 1 350 330Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceContainer),
            fillAlpha = 0.3f
        )
    }.build()
}