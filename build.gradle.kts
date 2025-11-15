plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("com.gradleup.shadow") version "+"
}

group = "me.sailex"
version = "1.0-SNAPSHOT"

application {
    mainClass = "me.sailex.mineskinproxy.MainKt"
}

repositories {
    mavenCentral()
    maven("https://repo.inventivetalent.org/repository/public/")
}

dependencies {
    implementation("org.mineskin:java-client:3.0.6")
    implementation("org.mineskin:java-client-java11:3.0.6")
    implementation("io.javalin:javalin:6.7.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
}

kotlin {
    jvmToolchain(21)
}