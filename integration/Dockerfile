FROM gradle:9.3.0-jdk17 AS builder

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle core/build.gradle.kts ./core/build.gradle.kts
COPY --chown=gradle:gradle administration/build.gradle.kts ./administration/build.gradle.kts
COPY --chown=gradle:gradle profile/build.gradle.kts ./profile/build.gradle.kts
COPY --chown=gradle:gradle integration/build.gradle.kts ./integration/build.gradle.kts
COPY --chown=gradle:gradle manager/build.gradle.kts ./manager/build.gradle.kts

RUN gradle :integration:dependencies \
    --configuration runtimeClasspath \
    --no-daemon

COPY --chown=gradle:gradle core/src ./core/src
COPY --chown=gradle:gradle integration/src ./integration/src

RUN gradle :integration:bootJar -x test --no-daemon

FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /opt/project

COPY --from=builder \
    /home/gradle/project/integration/build/libs/*.jar \
    ./app.jar

ENTRYPOINT ["java", "-jar", "/opt/project/app.jar"]
