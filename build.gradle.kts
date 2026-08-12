plugins {
    java
    checkstyle
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "ru.otus"
    version = "1.0-SNAPSHOT"
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

val checkstyleToolVersion = libs.versions.checkstyle.get()

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    checkstyle {
        toolVersion = checkstyleToolVersion
        configFile = resources.text.fromUri("https://raw.githubusercontent.com/OtusTeam/Spring/master/checkstyle.xml").asFile()
    }

    tasks.withType<Checkstyle>().configureEach {
        // Ignore checks in controllers
        setExcludes(listOf(
            "**/*PageController.java",
            "**/*Test.java"
        ))
    }
}
