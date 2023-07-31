plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")
    compileOnly("com.github.DevLeoko:AdvancedBan:2.3.0") {
        exclude(module = "AdvancedBan-Bukkit")
        exclude(module = "AdvancedBan-Bungee")
    }
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
}

tasks {
    processResources {
        val props = "version" to version
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("bungee.yml") {
            expand(props)
        }
    }
}
