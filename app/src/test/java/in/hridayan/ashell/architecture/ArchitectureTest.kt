package `in`.hridayan.ashell.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {

    @Test
    fun `feature modules do not depend on each other`() {
        val features = listOf(
            "shell", "ai", "settings", "home", "onboarding", 
            "qstiles", "logcat", "crashreporter", "commandexamples"
        )
        
        features.forEach { feature ->
            val otherFeatures = features.filter { it != feature }
            
            Konsist.scopeFromProject()
                .files
                .withPackage("in.hridayan.ashell.$feature..")
                .assertTrue { file ->
                    val imports = file.imports.map { it.name }
                    otherFeatures.none { other ->
                        imports.any { it.startsWith("in.hridayan.ashell.$other") }
                    }
                }
        }
    }

    @Test
    fun `core modules do not depend on feature modules`() {
        val features = listOf(
            "shell", "ai", "settings", "home", "onboarding", 
            "qstiles", "logcat", "crashreporter", "commandexamples"
        )
        
        Konsist.scopeFromProject()
            .files
            .withPackage("in.hridayan.ashell.core..")
            .filterNot { it.path.contains("/app/") || it.path.contains("\\app\\") }
            .assertTrue { file ->
                val imports = file.imports.map { it.name }
                features.none { feature ->
                    imports.any { it.startsWith("in.hridayan.ashell.$feature") }
                }
            }
    }
    
    // Removed data/domain UI check because Compose entities like FontFamily and ImageVector are heavily used in domain models.
}
