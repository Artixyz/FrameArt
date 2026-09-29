// Adaptador para 1.13 – 26.x (FILLED_MAP + MapMeta, molduras invisíveis/fixas).
dependencies {
    implementation(project(":platform-bukkit"))
    compileOnly("org.spigotmc:spigot-api:${property("modernApiVersion")}")
}
