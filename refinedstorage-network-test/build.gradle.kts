plugins {
    id("refinedarchitect.base")
}

refinedarchitect {
    testing()
    mutationTesting()
    javadoc()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("refinedstorage-network-test")
}

dependencies {
    api(project(":refinedstorage-network-api"))
    api(project(":refinedstorage-network"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-grid-api"))
    implementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
}
