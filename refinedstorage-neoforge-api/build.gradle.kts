plugins {
    id("com.refinedmods.refinedarchitect.neoforge")
}

refinedarchitect {
    neoForge()
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-common-api"))
}

base {
    archivesName.set("refinedstorage-neoforge-api")
}
