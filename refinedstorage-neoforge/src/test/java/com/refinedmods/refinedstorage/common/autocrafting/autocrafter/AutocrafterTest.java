package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.containerContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.createCraftingPattern;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.startAutocraftingTask;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.tickFurnace;
import static com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterTestPlots.createProcessingPattern;
import static com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterTestPlots.createSmithingTablePattern;
import static com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterTestPlots.createStoneCutterPattern;
import static com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterTestPlots.preparePlot;
import static net.minecraft.world.item.Items.AIR;
import static net.minecraft.world.item.Items.COAL;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.IRON_INGOT;
import static net.minecraft.world.item.Items.IRON_ORE;
import static net.minecraft.world.item.Items.IRON_PICKAXE;
import static net.minecraft.world.item.Items.NETHERITE_CHESTPLATE;
import static net.minecraft.world.item.Items.NETHERITE_INGOT;
import static net.minecraft.world.item.Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
import static net.minecraft.world.item.Items.OAK_LOG;
import static net.minecraft.world.item.Items.OAK_PLANKS;
import static net.minecraft.world.item.Items.STICK;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.item.Items.STONE_BRICKS;

public final class AutocrafterTest {
    private AutocrafterTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldCraftItem(final GameTestHelper helper) {
        preparePlot(helper, false, (autocrafter, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, OAK_LOG, 10)));

            // Act
            autocrafter.getPatternContainer().setItem(0,
                createCraftingPattern(List.of(OAK_LOG), List.of(0)));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, pos, new ResourceAmount(asResource(OAK_PLANKS), 4)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(OAK_PLANKS), 4)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldProcessItem(final GameTestHelper helper) {
        preparePlot(helper, true, (autocrafter, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, IRON_ORE, 10)));

            // Act
            autocrafter.getPatternContainer().setItem(0, createProcessingPattern(
                List.of(new ProcessingPatternState.ProcessingIngredient(
                    new ResourceAmount(asResource(IRON_ORE), 1), List.of()
                )),
                List.of(new ResourceAmount(asResource(IRON_INGOT), 1))));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, pos, new ResourceAmount(asResource(IRON_INGOT), 1)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(IRON_ORE), 9)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.below(),
                    new ResourceAmount(asResource(IRON_ORE), 1),
                    new ResourceAmount(asResource(COAL), 63)
                ))
                .thenExecute(() -> tickFurnace(helper, pos, 100))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(IRON_ORE), 9),
                    new ResourceAmount(asResource(IRON_INGOT), 1)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.below(),
                    new ResourceAmount(asResource(COAL), 63)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldProcessItemChained(final GameTestHelper helper) {
        preparePlot(helper, true, (autocrafter, pos, sequence) -> {
            // Arrange
            final BlockPos secondAutocrafterPos = pos.above();
            helper.setBlock(secondAutocrafterPos, MOD_BLOCKS.getAutocrafter().getDefault().rotated(Direction.DOWN));
            final AutocrafterBlockEntity secondAutocrafter =
                helper.getBlockEntity(secondAutocrafterPos, AutocrafterBlockEntity.class);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, IRON_ORE, 10)));

            // Act
            secondAutocrafter.getPatternContainer().setItem(0, createProcessingPattern(
                List.of(new ProcessingPatternState.ProcessingIngredient(
                    new ResourceAmount(asResource(IRON_ORE), 1), List.of()
                )),
                List.of(new ResourceAmount(asResource(IRON_INGOT), 1))));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, secondAutocrafterPos,
                    new ResourceAmount(asResource(IRON_INGOT), 1)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(IRON_ORE), 9)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.below(),
                    new ResourceAmount(asResource(IRON_ORE), 1),
                    new ResourceAmount(asResource(COAL), 63)
                ))
                .thenExecute(() -> tickFurnace(helper, pos, 100))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(IRON_ORE), 9),
                    new ResourceAmount(asResource(IRON_INGOT), 1)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.below(),
                    new ResourceAmount(asResource(COAL), 63)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldProcessAndCraftItem(final GameTestHelper helper) {
        preparePlot(helper, true, (autocrafter, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, IRON_ORE, 10);
                insert(helper, network, STICK, 2);
            }));

            // Act
            autocrafter.getPatternContainer().setItem(0, createProcessingPattern(
                List.of(new ProcessingPatternState.ProcessingIngredient(
                    new ResourceAmount(asResource(IRON_ORE), 1), List.of()
                )),
                List.of(new ResourceAmount(asResource(IRON_INGOT), 1))));
            autocrafter.getPatternContainer().setItem(1,
                createCraftingPattern(List.of(
                    IRON_INGOT, IRON_INGOT, IRON_INGOT,
                    AIR, STICK, AIR,
                    AIR, STICK, AIR
                ), List.of(0, 1, 2, 4, 7)));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, pos, new ResourceAmount(asResource(IRON_PICKAXE), 1)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.below(),
                    new ResourceAmount(asResource(IRON_ORE), 3),
                    new ResourceAmount(asResource(COAL), 63)
                ))
                .thenExecute(() -> tickFurnace(helper, pos, 100 * 3))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(IRON_ORE), 7),
                    new ResourceAmount(asResource(IRON_PICKAXE), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldCraftStoneCutterItem(final GameTestHelper helper) {
        preparePlot(helper, false, (autocrafter, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, STONE, 10)));

            // Act
            autocrafter.getPatternContainer().setItem(0,
                createStoneCutterPattern(asResource(STONE), asResource(STONE_BRICKS)));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, pos, new ResourceAmount(asResource(STONE_BRICKS), 1)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 9),
                    new ResourceAmount(asResource(STONE_BRICKS), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldCraftSmithingTableItem(final GameTestHelper helper) {
        preparePlot(helper, false, (autocrafter, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
                insert(helper, network, NETHERITE_INGOT, 1);
            }));

            // Act
            autocrafter.getPatternContainer().setItem(0, createSmithingTablePattern(
                asResource(NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                asResource(DIAMOND_CHESTPLATE),
                asResource(NETHERITE_INGOT)));

            // Assert
            sequence
                .thenExecute(startAutocraftingTask(helper, pos,
                    new ResourceAmount(asResource(NETHERITE_CHESTPLATE), 1)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(NETHERITE_UPGRADE_SMITHING_TEMPLATE), 1),
                    new ResourceAmount(asResource(NETHERITE_CHESTPLATE), 1)
                ))
                .thenSucceed();
        });
    }
}
