plugins {
    kotlin("jvm")
}


dependencies {
    api(project(":core"))

    implementation("p6spy:p6spy:3.8.2")
}
