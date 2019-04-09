plugins {
    kotlin("jvm")
}

dependencies {
    implementation("io.opencensus:opencensus-contrib-http-util:${project.extra["opencensusVersion"]}")

    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
    testImplementation("javax.servlet:javax.servlet-api:3.0.1")
}
