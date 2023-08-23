plugins {
    kotlin("jvm") version "1.6.20"
    application
}

tasks.withType<Wrapper> {
//    gradleVersion = "6.8.3"
    gradleVersion = "8.1.1"
}

group = "what.does.this.mean"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // SystemUnderTest
//    implementation(platform("org.http4k:http4k-bom:5.7.2.0"))
//    implementation("org.http4k:http4k-core")
//    implementation("org.http4k:http4k-server-undertow")
//    implementation("org.http4k:http4k-client-apache")


//    testImplementation(kotlin("test"))
//    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
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