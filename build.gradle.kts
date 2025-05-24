tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
