plugins {
    `java-library`
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    // Optional annotations (for @ApiStatus.Internal, etc.)
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.jar {
    archiveBaseName.set("fulcrum-api")
}
