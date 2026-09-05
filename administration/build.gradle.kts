plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.thymeleaf.extras.springsecurity6)
    implementation(libs.opencsv)
    implementation(libs.commons.io)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.security.test)
    testRuntimeOnly(libs.h2)
}

tasks.test {
    useJUnitPlatform()
}
