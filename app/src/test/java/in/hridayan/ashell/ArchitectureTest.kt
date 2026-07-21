package `in`.hridayan.ashell

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {

    private val features = listOf(
        "ai", "commandexamples", "crashreporter", "home",
        "logcat", "onboarding", "qstiles", "settings", "shell"
    )

    @Test
    fun `features should not depend on other features`() {
        features.forEach { featureName ->
            Konsist.scopeFromProduction()
                .classes()
                .filter { it.resideInPackage("in.hridayan.ashell.$featureName..") }
                .assertTrue { clazz ->
                    val otherFeatures = features.filter { it != featureName }
                    otherFeatures.none { otherFeature ->
                        clazz.containingFile.imports.any { import ->
                            import.name.startsWith("in.hridayan.ashell.$otherFeature.")
                        }
                    }
                }
        }
    }
}
