package parth.appdev.axiom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = DeepOrange,
    secondary = Amber,
    tertiary = Crimson,

    background = BackgroundDark,
    surface = SurfaceDark,

    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AxiomTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                letterSpacing = 1.5.sp
            )
        ),
        content = content
    )
}