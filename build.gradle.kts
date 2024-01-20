plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.2.2"
}

group = "me.aikovdp"
version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.DevLeoko:AdvancedBan:2.3.0") {
        exclude(module = "AdvancedBan-Bukkit")
        exclude(module = "AdvancedBan-Bungee")
    }
}

tasks {
    runServer {
        minecraftVersion("1.19.4")
        downloadPlugins {
            github("DevLeoko", "AdvancedBan", "v2.3.0", "AdvancedBan-Bundle-2.3.0-RELEASE.jar")
        }
    }

    processResources {
        val props = "version" to version
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
