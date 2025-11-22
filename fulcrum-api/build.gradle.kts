plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    // Optional annotations (for @ApiStatus.Internal, etc.)
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.16")
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.jar {
    archiveBaseName.set("fulcrum-api")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("io.github.BlackEden-Studios", "fulcrum-api", "0.1-BETA")

    // 4. Metadata required by Central
    pom {
        name.set("Fulcrum API")
        description.set("API for the Fulcrum Minecraft Framework")
        url.set("https://github.com/BlackEden-Studios/Fulcrum")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("BlackEden-Studios")
                name.set("BlackEden Studios")
                email.set("n.visci22@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/BlackEden-Studios/Fulcrum.git")
            developerConnection.set("scm:git:ssh://github.com/BlackEden-Studios/Fulcrum.git")
            url.set("https://github.com/BlackEden-Studios/Fulcrum")
        }
    }
}
