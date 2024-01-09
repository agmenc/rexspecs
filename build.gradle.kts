import org.jreleaser.model.Active

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
    `maven-publish`

    // TODO - upgrade to 1.9.0
    id("org.jreleaser") version "1.5.1"

    id("signing")
}

group = "io.github.agmenc"
version = "0.1.2-pre-alpha"

kotlin {
    jvmToolchain {
        // TODO - upgrade to 21
        languageVersion.set(JavaLanguageVersion.of(20))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // SystemUnderTest
    implementation(platform("org.http4k:http4k-bom:5.7.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-format-kotlinx-serialization")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jsoup:jsoup:1.16.1")

//    testImplementation(kotlin("test"))
//    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

//tasks.withType<KotlinCompile>() {
//    kotlinOptions.jvmTarget = "13"
//}

//application {
//    mainClassName = "MainKt"
//}

// -------------------------------------------------------------------------------------------------------------------
// Taken from https://dev.to/tschuehly/how-to-publish-a-kotlinjava-spring-boot-library-with-gradle-to-maven-central-complete-guide-402a#52-configure-jreleaser-maven-plugin
// -------------------------------------------------------------------------------------------------------------------
publishing{
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "io.github.agmenc"
            artifactId = "rexspecs"
            description = "Execute specification documents as tests"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("rexspecs")
                description.set("Executable Specifications")
                url.set("https://github.com/agmenc/rexspecs/")
                inceptionYear.set("2023")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("agmenc")
                        name.set("Chris Agmen-Smith")
                        email.set("chris.agmen-smith@email.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:agmenc/rexspecs.git")
                    developerConnection.set("scm:git:ssh:git@github.com:agmenc/rexspecs.git")
                    url.set("https://github.com/agmenc/rexspecs")
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.jar{
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}

jreleaser {
    project {
        copyright.set("Chris Agmen-Smith")
    }
    gitRootSearch.set(true)
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    active.set(Active.ALWAYS)
                    url.set("https://s01.oss.sonatype.org/service/local")
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------------------------
