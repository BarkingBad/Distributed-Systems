plugins {
    kotlin("jvm") version "1.3.61"
    id("org.gretty") version  "2.3.1"
    id("war")
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

val ktor_version: String by project
val slf4j_version: String by project
val gson_version: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("org.slf4j:slf4j-simple:$slf4j_version")
    implementation("com.google.code.gson:gson:$gson_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

kotlin {
    experimental {
        coroutines = org.jetbrains.kotlin.gradle.dsl.Coroutines.ENABLE
    }
}

buildscript {
    val gretty_version = "2.3.1"

    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("org.gretty:gretty:$gretty_version")
    }
}

gretty {
    contextPath = "/"
    servletContainer = "jetty9.4"
}


application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<Jar> {
    manifest {
        attributes(
                mapOf(
                        "Main-Class" to application.mainClassName
                )
        )
    }
}

