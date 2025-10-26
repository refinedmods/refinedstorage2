plugins {
    id("com.refinedmods.refinedarchitect.common")
}

refinedarchitect {
    common()
    javadoc()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("refinedstorage-common-api")
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-network-api"))
    api(project(":refinedstorage-autocrafting-api"))
}
