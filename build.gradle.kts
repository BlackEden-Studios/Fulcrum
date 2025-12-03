plugins {
    java
}

allprojects {
    group = "io.github.blackeden-studios"
    version = "0.1.3-BETA"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/creatorfromhell/")
        maven("https://maven.devs.beer/")
    }

}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}