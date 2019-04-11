plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))

    testImplementation(project(":opencensus"))
    testImplementation("io.opencensus:opencensus-exporter-trace-logging:${project.extra["opencensusVersion"]}")

    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
    testImplementation("javax.servlet:javax.servlet-api:${project.extra["servletApiVersion"]}")
}
