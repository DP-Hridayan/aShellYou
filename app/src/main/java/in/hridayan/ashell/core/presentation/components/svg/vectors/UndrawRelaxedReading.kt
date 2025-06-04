package `in`.hridayan.ashell.core.presentation.components.svg.vectors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors

@Composable
fun DynamicColorImageVectors.relaxedReading(): ImageVector {
    return Builder(
        name = "relaxedReading",
        defaultWidth = 924.83.dp,
        defaultHeight = 641.18.dp,
        viewportWidth = 924.83f,
        viewportHeight = 641.18f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M107.78,555.47S-9.85,501.32 31.47,463.76 107.78,555.47 107.78,555.47Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiary),
            fillAlpha = 0.5f
        )
        addPath(
            pathData = PathParser().parsePathString("M105.73,568.11S51.66,390.46 131.28,398.95 105.73,568.11 105.73,568.11Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiary),
            fillAlpha = 0.9f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M66.31,552.35l2.27,29.72 0.04,0.51a136.55,136.55 0,0 0,0.7 13.9c3.08,30.31 16.32,44.13 31.79,44.13s28.13,-13.81 31.21,-44.13a136.54,136.54 0,0 0,0.7 -13.9l0.02,-0.41 0.01,-0.1 2.28,-29.72Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString("M133.05,582.07l-0.01,0.1 -0.02,0.41a136.55,136.55 0,0 1,-0.7 13.9h-63a136.53,136.53 0,0 1,-0.7 -13.9l-0.04,-0.51Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
        )
        addPath(
            pathData = PathParser().parsePathString("M875.01,573.64s-91.07,-41.93 -59.08,-71S875.01,573.64 875.01,573.64Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiary),
            fillAlpha = 0.5f
        )
        addPath(
            pathData = PathParser().parsePathString("M873.42,583.43s-41.86,-137.54 19.78,-130.96S873.42,583.43 873.42,583.43Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiary),
            fillAlpha = 0.9f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M842.9,571.23l1.76,23.01 0.03,0.4a105.7,105.7 0,0 0,0.54 10.76c2.38,23.47 12.63,34.16 24.61,34.16s21.78,-10.7 24.17,-34.16a105.68,105.68 0,0 0,0.54 -10.76l0.01,-0.32 0.01,-0.08 1.76,-23.01Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString("M894.57,594.24l-0.01,0.08 -0.01,0.32a105.68,105.68 0,0 1,-0.54 10.76L845.23,605.39a105.68,105.68 0,0 1,-0.54 -10.76l-0.03,-0.4Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
        )
        addPath(
            pathData = PathParser().parsePathString("M306.6,191.25L306.6,150.83h-6.28v40.42a8.38,8.38 0,1 0,6.28 0Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.errorContainer),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M321.26,599.13L278.32,599.13L278.32,77.51L261.56,77.51v521.62L218.62,599.13A38.76,38.76 0,0 0,179.86 637.88L360.02,637.88a38.76,38.76 0,0 0,-38.76 -38.76Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M179.95,155.02L359.93,155.02L348.92,17.45a19.06,19.06 0,0 0,-18.9 -17.45L209.86,0a19.06,19.06 0,0 0,-18.9 17.45Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M430.55,396.47h0a12.96,12.96 0,0 0,-14.93 10.83l-5.07,33.25 17.24,4.57 12.63,-30.98a12.96,12.96 0,0 0,-9.87 -17.67Z"
            ).toNodes(),
            fill = SolidColor(Color(0xffffb9b9)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M430.23,440.49c9.57,38.02 5.45,72.8 -30.18,100.11 -42.73,-11.78 -71.16,-40.2 -88.5,-81.51a20.62,20.62 0,0 1,11.89 -27.33h0a20.61,20.61 0,0 1,22.69 5.85l41.93,48.4 19.67,-48.8Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M649.18,639.99c-39.02,-2.07 -63.26,-15.47 -69.13,-42.94l6.28,-11.52 71.95,32.78a10.06,10.06 0,0 1,-0.71 18.61Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiaryContainer),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M619.03,640.26c-20.27,-0.61 -39.77,-0.42 -58.25,0.83a12.34,12.34 0,0 1,-13.14 -11.33l-1.11,-13.86 -15.71,-25.14 13.62,-13.62c22.88,22.79 47.98,41.93 77.79,53.31a5.38,5.38 0,0 1,-3.19 9.81Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.tertiaryContainer),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M508.83,541.53C482.94,587.85 457.48,619.08 423.98,635.8c-41.2,13.6 -71.32,-4.42 -94.79,-41.37L397.28,549.38l9.95,9.95 90.18,-68.63a11.52,11.52 0,0 1,16.2 0.36l76.92,94.45 -9.43,15.71Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M531.87,595.99l-35.61,-54.47 8.38,-15.71L551.77,575.05Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
            strokeAlpha = 0.798f,
            fillAlpha = 0.798f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M336,452.5l-39.8,-13.62a49.43,49.43 0,0 0,7.33 -31.42h32.47C334.16,423.44 333.88,438.6 336,452.5Z"
            ).toNodes(),
            fill = SolidColor(Color(0xffffb9b9)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M410.99,565.23c-5.97,2.45 3.45,-7.03 -2.19,-4.33 -27.27,13.05 -56.13,23.64 -75.94,42.42 -46.29,-31.43 -89.19,-129.06 -33.52,-171.78 19,11.12 35.98,19.08 35.61,0Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M396.76,505.92l-19.38,-6.81 -38.23,29.85 -17.14,-61.7c-1.19,-4.28 -3.12,-2.11 -5.88,0.86a20.6,20.6 0,0 0,-34.57 14.2C279.52,527.08 293.02,564.94 326.58,593.9c43.9,-9.4 62.52,-39.06 70.18,-77.51l-2.08,-1.38Q395.83,510.55 396.76,505.92Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.onPrimary),
            strokeAlpha = 0.2f,
            fillAlpha = 0.2f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M398.32,459.22l32.59,38.32 48.89,-57.7 -33.03,-40.08Z"
            ).toNodes(),
            fill = SolidColor(Color(0xffffffff))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M471.44,441.16l-38.76,-31.71c2.2,-5.28 12.77,-3.52 12.77,-3.52l30.34,30.76a16.6,16.6 0,0 1,2.69 3.6h0Z"
            ).toNodes(),
            fill = SolidColor(Color(0xfffff5e4))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M473.2,438.96l-44.04,-34.35 -48.89,74L426.3,509.65c4.4,1.76 8.81,-1.32 8.81,-1.32l44.93,-68.71Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M434.23,415.91l0.42,-0.58l28.19,20.27l-0.42,0.58z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M426.22,499.11l37.02,-55.93l0.6,0.39l-37.02,55.93z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M391.95,479.32l0.42,-0.58l29.94,22.03l-0.42,0.58z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M390.98,471.35l38.32,-55.06l0.59,0.41l-38.32,55.06z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M440.84,405.82l0.3,-0.32l34.85,31.99l-0.3,0.32z"
            ).toNodes(),
            fill = SolidColor(Color(0xffb3b3b3))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M437.95,406.1l0.29,-0.33l36.43,32.16l-0.29,0.33z"
            ).toNodes(),
            fill = SolidColor(Color(0xffb3b3b3))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M435.67,406.75l0.28,-0.34l37.81,31.51l-0.28,0.34z"
            ).toNodes(),
            fill = SolidColor(Color(0xffb3b3b3))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M415.92,470.47h0a12.96,12.96 0,0 0,-18.14 3.38l-18.83,27.87 13.62,11.52L417.26,490.67a12.96,12.96 0,0 0,-1.34 -20.2Z"
            ).toNodes(),
            fill = SolidColor(Color(0xffffb9b9))
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M396.76,510.11c-7.65,38.46 -26.28,68.11 -70.18,77.51C293.02,558.65 279.52,520.79 281.56,476.04A20.62,20.62 0,0 1,304.02 456.44h0a20.61,20.61 0,0 1,17.99 15.01l17.14,61.7 38.69,-35.66Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M924.41,641.18L0,641.18v-2.29L924.83,638.9Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.onSurface),
            fillAlpha = 0.5f
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M570.3,107.37h-38.1a5.22,5.22 0,0 1,-4.53 -7.86l19.05,-32.99 0.05,-0.08a5.18,5.18 0,0 1,3.97 -2.51l0.03,0c0.16,-0.01 0.33,-0.02 0.49,-0.02a5.18,5.18 0,0 1,4.53 2.62L574.84,99.51a5.24,5.24 0,0 1,-4.53 7.86ZM531.15,102.12a1.08,1.08 0,0 0,0.14 0.53,1.03 1.03,0 0,0 0.91,0.52h38.1a1.03,1.03 0,0 0,0.91 -0.52,1.08 1.08,0 0,0 0.14,-0.53 1.03,1.03 0,0 0,-0.14 -0.52L552.16,68.61a1.05,1.05 0,0 0,-1.82 0L531.29,101.61A1.03,1.03 0,0 0,531.15 102.13Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M483.17,95.85a2.1,2.1 0,0 0,-2.1 2.1v163.4a2.1,2.1 0,0 0,2.1 2.1L619.33,263.44a2.1,2.1 0,0 0,2.1 -2.1v-163.4a2.1,2.1 0,0 0,-2.1 -2.1Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString("M497.31,247.2L605.2,247.2L605.2,112.08L497.31,112.08Z")
                .toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M768.97,141.41L748.31,141.41a2.83,2.83 0,0 1,-2.46 -4.26l10.33,-17.89 0.03,-0.04a2.81,2.81 0,0 1,2.15 -1.36h0.01c0.09,-0.01 0.18,-0.01 0.27,-0.01a2.81,2.81 0,0 1,2.46 1.42L771.43,137.15A2.84,2.84 0,0 1,768.97 141.41ZM747.74,138.57a0.58,0.58 0,0 0,0.08 0.29,0.56 0.56,0 0,0 0.49,0.28h20.66a0.56,0.56 0,0 0,0.49 -0.28,0.58 0.58,0 0,0 0.08,-0.29 0.56,0.56 0,0 0,-0.08 -0.28L759.13,120.4a0.57,0.57 0,0 0,-0.98 0l-10.33,17.89a0.56,0.56 0,0 0,-0.08 0.28Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M709.86,138.79a1.25,1.25 0,0 0,-1.25 1.25v81.29a1.25,1.25 0,0 0,1.25 1.25h97.55a1.25,1.25 0,0 0,1.25 -1.25L808.67,140.04a1.25,1.25 0,0 0,-1.25 -1.25Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M718.31,148.49L718.31,212.9h80.67L798.98,148.49Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.surface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M551.25,70.71m-3.14,0a3.14,3.14 0,1 1,6.28 0a3.14,3.14 0,1 1,-6.28 0"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M758.64,121.53m-1.7,0a1.7,1.7 0,1 1,3.41 0a1.7,1.7 0,1 1,-3.41 0"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M498.54,195.75a27.75,27.75 0,0 1,15.99 -15.01,27.42 27.42,0 0,1 22.85,1.42c8.17,4.34 14.71,11.16 23.01,15.31a58.84,58.84 0,0 0,25.96 6.12A58.11,58.11 0,0 0,615.46 196.03a1.57,1.57 0,0 0,-1.59 -2.71,55.87 55.87,0 0,1 -52.82,0.97c-8.33,-4.37 -14.93,-11.42 -23.41,-15.51a30.25,30.25 0,0 0,-23.28 -1.31A31.26,31.26 0,0 0,495.51 194.92c-0.76,1.87 2.28,2.69 3.03,0.83Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M496.06,225.12a119.62,119.62 0,0 1,52.94 0.18c15.47,3.59 31.43,10.04 47.38,5.23 7.12,-2.15 13.65,-6.45 17.31,-13.07 0.98,-1.77 -1.73,-3.36 -2.71,-1.59 -6.89,12.46 -23.04,14.56 -35.79,12.52 -8.52,-1.37 -16.73,-4.11 -25.12,-6.07a122.42,122.42 0,0 0,-54.85 -0.24C493.25,222.54 494.09,225.57 496.06,225.12Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M494.93,154.89a124.25,124.25 0,0 1,50.32 1.79c15.39,3.77 30.44,12.39 46.71,10.9 7.53,-0.69 14.99,-3.46 20.11,-9.22 1.34,-1.51 -0.88,-3.74 -2.22,-2.22 -4.42,4.97 -10.78,7.55 -17.3,8.25 -7.52,0.8 -14.91,-0.76 -22.07,-2.97 -7.93,-2.45 -15.62,-5.56 -23.68,-7.58a127.26,127.26 0,0 0,-52.7 -1.97c-1.99,0.34 -1.15,3.37 0.83,3.03Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M732.35,147.43a54.78,54.78 0,0 1,14.37 26.8,54.1 54.1,0 0,1 -11.7,45.94c-1.3,1.53 0.91,3.77 2.22,2.22a58.04,58.04 0,0 0,13.3 -30.03,57.29 57.29,0 0,0 -15.98,-47.15c-1.42,-1.45 -3.64,0.78 -2.22,2.22Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M779.5,140.72c-9.63,7.06 -15.02,18.46 -15.78,30.25 -0.79,12.27 3.89,24.12 11.43,33.64a65.42,65.42 0,0 0,15.55 14.04c1.7,1.11 3.27,-1.61 1.59,-2.71 -9.62,-6.3 -17.85,-15.24 -22.22,-25.97a42.02,42.02 0,0 1,-0.09 -32.2A34.4,34.4 0,0 1,781.09 143.43c1.61,-1.18 0.05,-3.91 -1.59,-2.71Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.secondary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M558.58,157.65m-14.66,0a14.66,14.66 0,1 1,29.33 0a14.66,14.66 0,1 1,-29.33 0"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.primary),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M289.04,383.86a36.32,36.32 49.01,1 0,72.46 5.08a36.32,36.32 49.01,1 0,-72.46 -5.08z"
            ).toNodes(),
            fill = SolidColor(Color(0xffffb9b9)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M239.57,335.18a32.13,32.13 49.01,1 0,64.1 4.5a32.13,32.13 49.01,1 0,-64.1 -4.5z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M242.12,362.03A32.13,32.13 49.01,0 0,292.78 348.68,32.13 32.13,49.01 1,1 231.18,331.15 32.12,32.12 49.01,0 0,242.12 362.03Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M368.6,370.94C363.71,360.53 361.86,356.2 355.08,349.59c-6,-5.84 -13.74,-7.97 -20.43,-3.31A38.22,38.22 49.01,1 0,355.12 378.38C360.68,378 363.05,371.33 368.6,370.94Z"
            ).toNodes(),
            fill = SolidColor(MaterialTheme.colorScheme.inverseSurface),
        )
    }.build()
}
