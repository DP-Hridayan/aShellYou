import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation

fun test() {
    val b = AnnotatedString.Builder()
    b.addLink(LinkAnnotation.Clickable("tag") { }, 0, 1)
}
