plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "NeoForge"
        url = project.uri("https://maven.neoforged.net/releases")
    }
    maven {
        name = "Fabric"
        url = project.uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation("net.neoforged:moddev-gradle:0.1.122")
    implementation("fabric-loom:fabric-loom.gradle.plugin:1.7-SNAPSHOT")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.8")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.4.1.3373")
    implementation("net.darkhax.curseforgegradle:CurseForgeGradle:1.1.18")
    implementation("com.modrinth.minotaur:Minotaur:2.8.7")
}
