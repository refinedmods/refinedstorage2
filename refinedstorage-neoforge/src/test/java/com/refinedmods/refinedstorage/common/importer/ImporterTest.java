package com.refinedmods.refinedstorage.common.importer;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeItem;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_ITEMS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.assertInterfaceEmpty;
import static com.refinedmods.refinedstorage.common.GameTestUtil.containerContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.interfaceContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.prepareChest;
import static com.refinedmods.refinedstorage.common.GameTestUtil.prepareInterface;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.importer.ImporterTestPlots.preparePlot;
import static net.minecraft.world.item.Items.COBBLESTONE;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class ImporterTest {
    private ImporterTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldImportItem(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance(),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

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

    @MinecraftIntegrationTest
    public static void shouldImportItemWithStackUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                COBBLESTONE.getDefaultInstance().copyWithCount(64),
                DIRT.getDefaultInstance()
            );

            importer.addUpgrade(MOD_ITEMS.getStackUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenExecute(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 1)
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(COBBLESTONE), 64)
                ))
                .thenIdle(9)
                .thenExecute(() -> helper.assertContainerEmpty(pos.east()))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(COBBLESTONE), 64)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldImportItemWithRegulatorUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                COBBLESTONE.getDefaultInstance(),
                DIRT.getDefaultInstance().copyWithCount(15)
            );

            final ItemStack upgrade = MOD_ITEMS.getRegulatorUpgrade().getDefaultInstance();
            if (upgrade.getItem() instanceof RegulatorUpgradeItem upgradeItem) {
                upgradeItem.setAmount(upgrade, asResource(DIRT.getDefaultInstance()), 10);
            }
            importer.addUpgrade(upgrade);

            // Assert
            sequence
                .thenIdle(95)
                .thenExecute(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 15),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(COBBLESTONE), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldImportItemBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance(),
                DIAMOND_CHESTPLATE.getDefaultInstance(),
                damagedDiamondChestplate
            );

            importer.setFuzzyMode(false);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1)
                ))
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

    @MinecraftIntegrationTest
    public static void shouldImportItemFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance(),
                DIAMOND_CHESTPLATE.getDefaultInstance(),
                damagedDiamondChestplate
            );

            importer.setFuzzyMode(true);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenIdle(9)
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldImportItemAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance(),
                DIAMOND_CHESTPLATE.getDefaultInstance(),
                damagedDiamondChestplate
            );

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
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
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

    @MinecraftIntegrationTest
    public static void shouldImportItemFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance(),
                DIAMOND_CHESTPLATE.getDefaultInstance(),
                damagedDiamondChestplate
            );

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

    @MinecraftIntegrationTest
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

    @MinecraftIntegrationTest
    public static void shouldImportFluidWithStackUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 15),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
            );
            importer.addUpgrade(MOD_ITEMS.getStackUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenExecute(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 63)
                ))
                .thenIdle(9)
                .thenExecute(assertInterfaceEmpty(helper, pos.east()))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 63),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenSucceed();
        });
    }
}
