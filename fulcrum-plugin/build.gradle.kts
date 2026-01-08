plugins {
    java
    alias(libs.plugins.shadow) // Replaces id("...") version "..."
}

dependencies {
    implementation(project(":fulcrum-api"))

    // Database
    implementation(libs.jedis)

    // Platform (Paper)
    compileOnly(libs.paper.api)

    // Integrations
    compileOnly(libs.vault.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.lands.api)
    compileOnly(libs.itemsadder.api)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

tasks.shadowJar {
    archiveBaseName.set("Fulcrum")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}