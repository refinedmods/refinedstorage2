plugins {
    id("com.refinedmods.refinedarchitect.base")
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
    implementation(libs.slf4j.api)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(project(":refinedstorage-network-test"))
    testRuntimeOnly(libs.slf4j.impl)
    testRuntimeOnly(libs.junit.engine)
}
