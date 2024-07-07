plugins {
    id("jacoco")
    id("java-library")
    id("checkstyle")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

version = "0.0.0"

if (System.getenv("GITHUB_SHA") != null) {
    version = "0.0.0+" + System.getenv("GITHUB_SHA").substring(0, 7)
}

if (System.getenv("RELEASE_VERSION") != null) {
    version = System.getenv("RELEASE_VERSION")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
    options.isDeprecation = true
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("999")
}

checkstyle {
    toolVersion = "10.17.0"
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand("version" to project.version)
    }
}
