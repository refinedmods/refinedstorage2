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
        .layer("Entrypoint").definedBy("com.refinedmods.refinedstorage2.fabric")
        .layer("Registration").definedBy("com.refinedmods.refinedstorage2.fabric.init..")

        .layer("Blocks").definedBy("com.refinedmods.refinedstorage2.fabric.block")
        .layer("Block Entities").definedBy("com.refinedmods.refinedstorage2.fabric.block.entity")
        .layer("Items").definedBy("com.refinedmods.refinedstorage2.fabric.item..")

        .layer("Core Impl").definedBy("com.refinedmods.refinedstorage2.fabric.coreimpl..")

        .layer("Packets").definedBy("com.refinedmods.refinedstorage2.fabric.packet..")

        .whereLayer("Entrypoint").mayOnlyBeAccessedByLayers("Registration", "Packets", "Blocks", "Block Entities", "Items")
        .whereLayer("Registration").mayOnlyBeAccessedByLayers(
            "Entrypoint",
            "Block Entities" // block entities refer to type
        )

        .whereLayer("Blocks").mayOnlyBeAccessedByLayers("Registration")
        .whereLayer("Block Entities").mayOnlyBeAccessedByLayers("Registration", "Blocks")
        .whereLayer("Items").mayOnlyBeAccessedByLayers("Registration")

        .whereLayer("Core Impl").mayOnlyBeAccessedByLayers(
            "Registration", // to get the storage disk types
            "Entrypoint",
            "Blocks",
            "Block Entities",
            "Items"
        )

        .whereLayer("Packets").mayOnlyBeAccessedByLayers("Core Impl", "Entrypoint");
}
