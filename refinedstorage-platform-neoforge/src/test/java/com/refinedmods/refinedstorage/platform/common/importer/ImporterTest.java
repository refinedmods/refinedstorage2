package com.refinedmods.refinedstorage.platform.common.importer;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.containerContainsExactly;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.platform.common.importer.ImporterTestPlots.prepareChest;
import static com.refinedmods.refinedstorage.platform.common.importer.ImporterTestPlots.preparePlot;
import static net.minecraft.world.item.Items.COBBLESTONE;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.WATER;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ImporterTest {
    private ImporterTest() {
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportItem(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(),
                    COBBLESTONE.getDefaultInstance().copyWithCount(3));

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertContainerEmpty(pos.east()))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 11),
                            new ResourceAmount(asResource(STONE), 15),
                            new ResourceAmount(asResource(COBBLESTONE), 3)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportItemBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(),
                    DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            importer.setFuzzyMode(false);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                    .thenWaitUntil(containerContainsExactly(
                            helper,
                            pos.east(),
                            new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1)))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 11),
                            new ResourceAmount(asResource(STONE), 15),
                            new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportItemFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(),
                    DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            importer.setFuzzyMode(true);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                    .thenIdle(10)
                    .thenWaitUntil(containerContainsExactly(
                            helper,
                            pos.east(),
                            new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1),
                            new ResourceAmount(asResource(damagedDiamondChestplate), 1)))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 11),
                            new ResourceAmount(asResource(STONE), 15)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportItemAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(),
                    DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            importer.setFuzzyMode(false);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertContainerContains(pos.east(), DIRT))
                    .thenWaitUntil(containerContainsExactly(
                            helper,
                            pos.east(),
                            new ResourceAmount(asResource(DIRT), 1),
                            new ResourceAmount(asResource(damagedDiamondChestplate), 1)))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 10),
                            new ResourceAmount(asResource(STONE), 15),
                            new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportItemFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(),
                    DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            importer.setFuzzyMode(true);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertContainerContains(pos.east(), DIRT))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 10),
                            new ResourceAmount(asResource(STONE), 15),
                            new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1),
                            new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportFluid(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            helper.setBlock(pos.east(), Blocks.WATER_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL));

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.CAULDRON, pos.east()))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 10),
                            new ResourceAmount(asResource(STONE), 15),
                            new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                    ))
                    .thenSucceed();
        });
    }
}
