plugins {
    // Baixa automaticamente o JDK 25 caso não exista localmente.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "FrameArt"

include(
    "core",
    "infrastructure",
    "platform-bukkit",
    "adapter-legacy",
    "adapter-modern",
    "plugin",
)
