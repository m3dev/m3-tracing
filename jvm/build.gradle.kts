plugins {
    base
    java
    kotlin("jvm") version "1.3.21" apply false
}

allprojects {
    group = "com.m3.tracing"
    version = "1.0"

    repositories {
        jcenter()
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    val opencensusVersion by extra { "0.20.0" }

    dependencies {
        compile(kotlin("stdlib"))

        implementation("io.opencensus:opencensus-api:${project.extra["opencensusVersion"]}")
        implementation("io.opencensus:opencensus-impl:${project.extra["opencensusVersion"]}")
        testImplementation("io.opencensus:opencensus-exporter-trace-logging:${project.extra["opencensusVersion"]}")

        implementation("org.slf4j:slf4j-api:1.7.26")
        testImplementation("org.slf4j:jul-to-slf4j:1.7.26")
        testImplementation("ch.qos.logback:logback-classic:1.2.3")

        testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
        testImplementation("org.mockito:mockito-junit-jupiter:2.26.0")

    }
}
