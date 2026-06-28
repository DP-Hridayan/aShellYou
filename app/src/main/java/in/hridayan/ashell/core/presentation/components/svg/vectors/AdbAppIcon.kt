package `in`.hridayan.ashell.core.presentation.components.svg.vectors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors

/**
 * ADB app icon as a Compose [ImageVector] with an animatable group hierarchy.
 *
 * Animating [headGroup] moves the full head assembly (shape, antennas, eyes) together.
 * Each child group can additionally carry its own independent animation on top.
 *
 * The circle background from the original `animated_adb_splash_icon.xml` is intentionally omitted.
 */
@Composable
fun DynamicColorImageVectors.adbAppIcon(): ImageVector {
    val foreground = SolidColor(MaterialTheme.colorScheme.primary)
    val eyeFill = SolidColor(MaterialTheme.colorScheme.onPrimary)

    return Builder(
        name = "adbAppIcon",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {

        group(
            name = "headGroup",
            pivotX = 24f,
            pivotY = 22f
        ) {
            group(
                name = "antennaGroup",
                pivotX = 24f,
                pivotY = 6f
            ) {
                addPath(
                    pathData = PathParser().parsePathString(
                        "M10 21.95v-2" +
                                "q0-3.6 1.65-6.55Q13.3 10.45 16 8.55" +
                                "L12.25 4.8l1.8-1.8 4.25 4.2" +
                                "q1.25-0.6 2.725-0.925Q22.5 5.95 24 5.95" +
                                "t2.975 0.325Q28.45 6.6 29.75 7.2" +
                                "l4.2-4.2 1.8 1.8L32 8.55" +
                                "q2.7 1.9 4.35 4.85Q38 16.35 38 19.95v2z"
                    ).toNodes(),
                    fill = foreground
                )
            }

            group(
                name = "rightEyeGroup",
                pivotX = 30f,
                pivotY = 15.95f
            ) {
                addPath(
                    pathData = PathParser().parsePathString(
                        "M30 17.95" +
                                "q0.85 0 1.425-0.575Q32 16.8 32 15.95" +
                                "q0-0.85-0.575-1.425Q30.85 13.95 30 13.95" +
                                "q-0.85 0-1.425 0.575Q28 15.1 28 15.95" +
                                "q0 0.85 0.575 1.425Q29.15 17.95 30 17.95z"
                    ).toNodes(),
                    fill = eyeFill
                )
            }

            group(
                name = "leftEyeGroup",
                pivotX = 18f,
                pivotY = 15.95f
            ) {
                addPath(
                    pathData = PathParser().parsePathString(
                        "M18 17.95" +
                                "q0.85 0 1.425-0.575Q20 16.8 20 15.95" +
                                "q0-0.85-0.575-1.425Q18.85 13.95 18 13.95" +
                                "q-0.85 0-1.425 0.575Q16 15.1 16 15.95" +
                                "q0 0.85 0.575 1.425Q17.15 17.95 18 17.95z"
                    ).toNodes(),
                    fill = eyeFill
                )
            }
        }

        group(
            name = "bodyGroup",
            pivotX = 24f,
            pivotY = 24f
        ) {
            addPath(
                pathData = PathParser().parsePathString(
                    "M24 46" +
                            "q-5.85 0-9.925-4.075Q10 37.85 10 32v-8.05h28V32" +
                            "q0 5.85-4.075 9.925Q29.85 46 24 46z"
                ).toNodes(),
                fill = foreground
            )
        }

    }.build()
}