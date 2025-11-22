plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":fulcrum-api"))
    implementation("redis.clients:jedis:7.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.16")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.angeschossen:LandsAPI:7.19.1")
    compileOnly("dev.lone:api-itemsadder:4.0.10")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
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
