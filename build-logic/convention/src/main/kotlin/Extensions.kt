import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.githubImplementations(vararg dependencyNotations: Any) {
    dependencyNotations.forEach { add("githubImplementation", it) }
}