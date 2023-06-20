
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    java
    `maven-publish`
    kotlin("jvm") version "1.7.22" apply false
}

allprojects {
    group = "com.m3.tracing"
    version = "2.0.1"

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
    apply(plugin = "maven-publish")

    tasks.withType<KotlinCompile> {
        kotlinOptions.javaParameters = true
        kotlinOptions.freeCompilerArgs = listOf("-progressive", "-Xjvm-default=enable")
        kotlinOptions.jvmTarget = "17"
    }

    val sourcesJar by tasks.creating(Jar::class) {
      classifier = "sources"
      from(sourceSets["main"].allSource)
    }

    val opencensusVersion by extra { "0.25.0" }

    val servletApiVersion by extra { "6.0.0" }

    // Following versions are based on spring-boot
    val springBootVersion by extra { "3.0.0" }
    val springVersion by extra { "6.0.0" }
    val apacheHttpClientVersion by extra { "4.5.8" }
    val okHttpClientVersion by extra { "4.8.0" }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.STARTED, TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        }
    }

    dependencies {
        implementation("com.google.code.findbugs:jsr305:3.0.2")
        implementation("com.google.guava:guava:27.1-jre")

        implementation("org.slf4j:slf4j-api:1.7.26")
        testImplementation("org.slf4j:jul-to-slf4j:1.7.26")
        testImplementation("ch.qos.logback:logback-classic:1.2.3")

        testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
        testImplementation("org.mockito:mockito-junit-jupiter:4.8.1")
        testImplementation("org.mockito:mockito-core:4.8.1")
        testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
        testImplementation("com.google.truth:truth:0.44")
        testImplementation("com.google.truth.extensions:truth-java8-extension:0.44")
    }

    publishing {
        repositories {
            maven {
                // change to point to your repo, e.g. http://my.org/repo
                if (version.toString().endsWith("SNAPSHOT")) {
                    url = uri("https://packages.m3internal.com/repository/maven-snapshots/")
                } else {
                    url = uri("https://packages.m3internal.com/repository/maven-releases/")
                }
            }
        }

        // Legacy billing plans can't use GitHub Package Registry
        /*
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/m3dev/m3-tracing")
                credentials {
                    username = System.getenv("GITHUB_USERNAME")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        */

        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(sourcesJar)

                pom {
                    url.set("https://github.com/m3dev/m3-tracing")

                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://github.com/m3dev/m3-tracing/blob/master/LICENSE")
                        }
                    }
                }
            }
        }
    }

}
