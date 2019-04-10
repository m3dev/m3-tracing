plugins {
    kotlin("jvm")
}


dependencies {
    api(project(":core"))

    implementation("io.opencensus:opencensus-api:${project.extra["opencensusVersion"]}")
    implementation("io.opencensus:opencensus-impl:${project.extra["opencensusVersion"]}")
    implementation("io.opencensus:opencensus-contrib-http-util:${project.extra["opencensusVersion"]}")
    testImplementation("io.opencensus:opencensus-exporter-trace-logging:${project.extra["opencensusVersion"]}")
}
