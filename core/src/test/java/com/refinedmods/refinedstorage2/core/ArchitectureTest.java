package com.refinedmods.refinedstorage2.core;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RefinedStorage2Test
@AnalyzeClasses(packages = "com.refinedmods.refinedstorage2.core", importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {
    @ArchTest
    public static final ArchRule stayFairlyIndependentFromMinecraft = noClasses().should().dependOnClassesThat(
        resideInAPackage("net.minecraft..")
            .and(are(not(assignableTo(BlockPos.class))))
            .and(are(not(assignableTo(ItemStack.class))))
            .and(are(not(assignableTo(Item.class))))
            .and(are(not(assignableTo(Direction.class))))
            .and(are(not(assignableTo(BlockEntity.class))))
            .and(are(not(assignableTo(CompoundTag.class))))
            .and(are(not(assignableTo(ServerWorld.class))))
            .and(are(not(assignableTo(World.class))))
            .and(are(not(assignableTo(MinecraftServer.class))))
    ).because("we want the core module to stay fairly independent from Minecraft by only using essential classes");

    @ArchTest
    public static final ArchRule dontUseAnyFabricClasses = noClasses().should()
        .dependOnClassesThat()
        .resideInAPackage("net.fabricmc..")
        .because("the core module should stay independent from Fabric, we are only including Fabric Loader to fix build errors");
}
