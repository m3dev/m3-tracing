plugins {
    kotlin("jvm")

    // For spring-boot
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.21"
}

dependencies {
    api(project(":core"))
    api(project(":spring-web"))
    api(project(":servlet"))

    implementation("javax.servlet:javax.servlet-api:${project.extra["servletApiVersion"]}")
    implementation("org.springframework.boot:spring-boot:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${project.extra["springBootVersion"]}")
}
