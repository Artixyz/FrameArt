// Adaptador para 1.8 – 1.12.2 (IDs de mapa em short, Material.MAP + durabilidade).
dependencies {
    implementation(project(":platform-bukkit"))
    compileOnly("org.spigotmc:spigot-api:${property("legacyApiVersion")}")
}
