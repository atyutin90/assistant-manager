plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.kafka)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.kafka.test)
}

tasks.test {
    useJUnitPlatform()
}
