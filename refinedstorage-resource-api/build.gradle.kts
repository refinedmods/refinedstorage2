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
    archivesName.set("refinedstorage-resource-api")
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-core-api"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
}
