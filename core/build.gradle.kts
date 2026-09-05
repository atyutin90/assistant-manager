plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.kafka)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.jjwt.api)
    implementation(libs.jjwt.impl)
    implementation(libs.jjwt.jackson)
    implementation(libs.opencsv)
    implementation(libs.reflections)
    implementation(libs.commons.lang3)
    compileOnly(libs.lombok)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
