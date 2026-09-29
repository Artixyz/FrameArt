// Composition root: junta todos os módulos em um único JAR do plugin.
dependencies {
    implementation(project(":core"))
    implementation(project(":infrastructure"))
    implementation(project(":platform-bukkit"))
    implementation(project(":adapter-legacy"))
    implementation(project(":adapter-modern"))
    compileOnly("org.spigotmc:spigot-api:${property("legacyApiVersion")}")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.jar {
    archiveBaseName.set("FrameArt")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Nenhuma lib externa é empacotada: apenas os módulos do próprio projeto.
    val runtime = configurations.runtimeClasspath
    dependsOn(runtime)
    from({ runtime.get().map { if (it.isDirectory) it else zipTree(it) } })
}
