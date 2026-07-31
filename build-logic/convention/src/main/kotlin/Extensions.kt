import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.githubImplementation(dependencyNotation: Any) {
    add("githubImplementation", dependencyNotation)
}

fun DependencyHandler.githubImplementations(vararg dependencyNotations: Any) {
    dependencyNotations.forEach { add("githubImplementation", it) }
}
