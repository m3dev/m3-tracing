plugins {
    kotlin("jvm")

    // For spring-boot
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.22"
}

dependencies {
    api(project(":core"))
    api(project(":spring-web"))
    api(project(":servlet"))

    // Caution: Should not expose this dependency to publised POM to avoid overriding servlet-api dependency from spring-boot.
    compileOnly("jakarta.servlet:jakarta.servlet-api:${project.extra["servletApiVersion"]}")

    implementation("org.springframework:spring-web:${project.extra["springVersion"]}")
    implementation("org.springframework:spring-aspects:${project.extra["springVersion"]}")
    implementation("org.springframework.boot:spring-boot:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${project.extra["springBootVersion"]}")
}
