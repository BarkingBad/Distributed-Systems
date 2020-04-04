plugins {
    kotlin("jvm") version "1.3.71"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.rabbitmq:amqp-client:latest.release")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("com.google.code.gson:gson:2.7")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

task("administrator", JavaExec::class) {
    main = "administrator.AdministratorKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

task("agency", JavaExec::class) {
    main = "agency.AgencyKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

task("carrier", JavaExec::class) {
    main = "carrier.CarrierKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}