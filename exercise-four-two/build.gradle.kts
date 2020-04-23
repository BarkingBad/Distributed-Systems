plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jruyi.thrift") version "0.4.1"
    scala
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.scala-lang:scala-library:2.11.12")
    implementation("org.apache.thrift:libthrift:0.11.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    implementation("org.slf4j:slf4j-simple:1.7.25")

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileThrift {
        generator("java", "private-members")
    }
}

