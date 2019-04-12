plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))

    implementation("org.apache.httpcomponents:httpclient:${project.extra["apacheHttpClientVersion"]}")
}
