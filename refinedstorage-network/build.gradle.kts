plugins {
    id("refinedarchitect.base")
}

refinedarchitect {
    testing()
    mutationTesting()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("refinedstorage-network")
}

dependencies {
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-network-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-grid-api"))
    implementation(libs.slf4j.api)
    testRuntimeOnly(libs.slf4j.impl)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(project(":refinedstorage-network-test"))
}
