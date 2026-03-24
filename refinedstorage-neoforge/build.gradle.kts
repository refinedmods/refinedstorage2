plugins {
    id("com.refinedmods.refinedarchitect.neoforge")
}

refinedarchitect {
    modId = "refinedstorage"
    neoForge()
    gameTests()
    dataGeneration(project(":refinedstorage-common"))
    publishing {
        maven = true
        curseForge = "243076"
        modrinth = "KDvYkUg3"
    }
}

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    testCompileOnly(libs.apiguardian)
    testCompileOnly(project(":refinedstorage-common"))
    testCompileOnly("org.ojalgo:ojalgo:55.2.0")

    compileOnly(project(":refinedstorage-common"))
    compileOnly(project(":refinedstorage-common-api"))
    compileOnly(project(":refinedstorage-neoforge-api"))
    compileOnly(project(":refinedstorage-core-api"))
    compileOnly(project(":refinedstorage-resource-api"))
    compileOnly(project(":refinedstorage-storage-api"))
    compileOnly(project(":refinedstorage-network-api"))
    compileOnly(project(":refinedstorage-network"))
    compileOnly(project(":refinedstorage-autocrafting-api"))
    compileOnly(project(":refinedstorage-query-parser"))
    compileOnly("org.ojalgo:ojalgo:55.2.0")
    commonJava(project(path = ":refinedstorage-common", configuration = "commonJava"))
    commonResources(project(path = ":refinedstorage-common", configuration = "commonResources"))
    commonJava(project(path = ":refinedstorage-common-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-neoforge-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-core-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-resource-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-storage-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-network-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-network", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-autocrafting-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-query-parser", configuration = "commonJava"))
    commonJava("org.ojalgo:ojalgo:55.2.0")
}

base {
    archivesName.set("refinedstorage-neoforge")
}
