pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "assistant-manager"

include("administration", "anketa", "core", "integration", "manager")
