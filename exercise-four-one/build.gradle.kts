import com.google.protobuf.gradle.*

val kotlin_version: String by project
val coroutines_version: String by project
val protobuf_version: String by project
val grpc_version: String by project
val grpc_kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.3.72"
    scala
    id("com.google.protobuf") version "0.8.11"
    idea
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    jcenter()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.grpc:grpc-kotlin-stub:$grpc_kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("com.google.protobuf:protobuf-java:$protobuf_version")
    implementation("com.google.protobuf:protobuf-java-util:$protobuf_version")
    implementation("io.grpc:grpc-netty-shaded:$grpc_version")
    implementation("io.grpc:grpc-protobuf:$grpc_version")
    implementation("io.grpc:grpc-stub:$grpc_version")
    compileOnly("javax.annotation:javax.annotation-api:1.2")
    implementation("com.google.guava:guava:28.2-jre")
    implementation("org.scala-lang:scala-library:2.11.12")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protobuf_version" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpc_version"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpc_kotlin_version"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
