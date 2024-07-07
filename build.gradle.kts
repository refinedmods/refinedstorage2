plugins {
    id("refinedarchitect.root")
}

refinedarchitect {
    sonarQube("refinedmods_refinedstorage2", "refinedmods")
}

subprojects {
    group = "com.refinedmods.refinedstorage"
}
