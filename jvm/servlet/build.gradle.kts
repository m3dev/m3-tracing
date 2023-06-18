plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))

    testImplementation(project(":opencensus"))
    testImplementation("io.opencensus:opencensus-exporter-trace-logging:${project.extra["opencensusVersion"]}")

    // Should not expose this dependency to prevent affecting serlvet API version of user's application
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    testImplementation("jakarta.servlet:jakarta.servlet-api:${project.extra["servletApiVersion"]}")
}
