plugins {
    kotlin("jvm") version "1.3.61"
}

group = "distributed-systems"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

task("server", JavaExec::class) {
    main = "server.ServerKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

task("client", JavaExec::class) {
    main = "client.ClientKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

task("serverJar", Jar::class) {
    archiveClassifier.set("serverJar")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)

    manifest {
        attributes["Main-Class"] = "server.ServerKt"
    }

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

task("clientJar", Jar::class) {
    archiveClassifier.set("clientJar")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)

    manifest {
        attributes["Main-Class"] = "client.ClientKt"
    }

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}




