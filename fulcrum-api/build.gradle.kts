plugins {
    `java-library`
    alias(libs.plugins.maven.publish) // Replaces id("...") version "..."
}

dependencies {
    // Paper API
    compileOnly(libs.paper.api)

    // Annotations
    compileOnly(libs.jetbrains.annotations)

    // Integrations
    compileOnly(libs.vault.api)

    // Testing
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("fulcrum-api")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group as String?, "fulcrum-api", version as String?)

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