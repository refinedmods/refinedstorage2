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
    archivesName.set("refinedstorage-core-api")
}

dependencies {
    api(libs.apiguardian)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.engine)
}
