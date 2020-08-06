plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))

    implementation("com.squareup.okhttp3:okhttp:${project.extra["okHttpClientVersion"]}")
}
