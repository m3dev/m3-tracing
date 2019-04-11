plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))

    implementation("org.springframework:spring-web:${project.extra["springWebVersion"]}")
}
