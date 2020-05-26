group = "org.example"
version = "1.0-SNAPSHOT"

plugins {
//    kotlin("jvm") version "1.3.72"
    scala
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
}

dependencies {
//    implementation(kotlin("stdlib-jdk8"))
    implementation("org.scala-lang:scala-library:2.13.1")

    implementation("com.typesafe.akka:akka-actor_2.13:2.6.5")
    implementation("com.typesafe.akka:akka-stream_2.13:2.6.5")
    implementation("com.typesafe.akka:akka-http-core_2.13:10.1.12")
    implementation("com.typesafe.akka:akka-http_2.13:10.1.12")
    implementation("com.typesafe.akka:akka-http-spray-json_2.13:10.1.12")
    implementation("com.typesafe.akka:akka-slf4j_2.13:2.6.5")
    implementation("org.slf4j:slf4j-simple:1.6.2")
    implementation("com.typesafe.akka:akka-remote_2.13:2.6.5")
    implementation("com.typesafe.akka:akka-persistence_2.13:2.6.5")

    implementation("com.typesafe.slick:slick_2.13:3.3.2")
    implementation("com.typesafe.slick:slick-hikaricp_2.13:3.3.2")
    implementation("org.xerial:sqlite-jdbc:3.31.1")

    implementation("org.jsoup:jsoup:1.13.1")
}