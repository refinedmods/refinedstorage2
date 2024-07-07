plugins {
    id("refinedarchitect.base")
}

refinedarchitect {
    javadoc()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("refinedstorage-network-api")
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-grid-api"))
}
