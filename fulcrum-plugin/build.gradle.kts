plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":fulcrum-api"))
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    archiveBaseName.set("Fulcrum")
    archiveClassifier.set("") // produce Fulcrum.jar
    archiveVersion.set("")

    // Optional: relocate internal packages if you bundle external libs
    // relocate("com.bestudios.fulcrum.api", "com.bestudios.fulcrum.internal")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
