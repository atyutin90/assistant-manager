pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "assistant-manager"

include("administration", "profile", "core", "integration", "manager")
