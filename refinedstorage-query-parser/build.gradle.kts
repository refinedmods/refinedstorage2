plugins {
    id("com.refinedmods.refinedarchitect.base")
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
    archivesName.set("refinedstorage-query-parser")
}

dependencies {
    api(project(":refinedstorage-core-api"))
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.engine)
}
