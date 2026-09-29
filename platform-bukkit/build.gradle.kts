// Adaptadores Bukkit comuns a todas as versões. Compilado contra a API 1.8.8 (menor denominador comum).
dependencies {
    api(project(":core"))
    compileOnly("org.spigotmc:spigot-api:${property("legacyApiVersion")}")
}
