package com.refinedmods.refinedstorage2.fabric;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.refinedmods.refinedstorage2.fabric", importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {
    @ArchTest
    public static final ArchRule layers = layeredArchitecture()
        .layer("Blocks").definedBy("com.refinedmods.refinedstorage2.fabric.block")
        .layer("Block Entities").definedBy("com.refinedmods.refinedstorage2.fabric.block.entity")
        .layer("Core Impl").definedBy("com.refinedmods.refinedstorage2.fabric.coreimpl..")
        .layer("Init").definedBy("com.refinedmods.refinedstorage2.fabric.init..")
        .layer("Entrypoint").definedBy("com.refinedmods.refinedstorage2.fabric")
        .whereLayer("Blocks").mayOnlyBeAccessedByLayers("Init", "Entrypoint")
        .whereLayer("Block Entities").mayOnlyBeAccessedByLayers("Init", "Entrypoint", "Blocks")
        .whereLayer("Init").mayOnlyBeAccessedByLayers("Entrypoint", "Blocks", "Block Entities")
        .whereLayer("Core Impl").mayOnlyBeAccessedByLayers("Entrypoint", "Blocks", "Block Entities");
}
