package com.refinedmods.refinedstorage2.platform.common.importer;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

import static com.refinedmods.refinedstorage2.platform.common.importer.ImporterTestPlots.prepareChest;
import static com.refinedmods.refinedstorage2.platform.common.importer.ImporterTestPlots.preparePlot;
import static com.refinedmods.refinedstorage2.platform.forge.GameTestUtil.*;
import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.level.material.Fluids.WATER;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ImporterTest {
    private ImporterTest() {
    }

    @GameTest(template = "empty_15x15")
    public static void shouldImportBlock(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            prepareChest(helper, pos.east(), DIRT.getDefaultInstance(), COBBLESTONE.getDefaultInstance().copyWithCount(3));

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

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
    public static void shouldImportBlockFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), Items.DIRT.getDefaultInstance(), DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            importer.setFuzzyMode(true);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                    .thenWaitUntil(() -> assertContainerContains(helper, pos.east(), DIAMOND_CHESTPLATE.getDefaultInstance()))
                    .thenWaitUntil(() -> assertContainerContains(helper, pos.east(), damagedDiamondChestplate))
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
    public static void shouldImportBlockFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);
            prepareChest(helper, pos.east(), Items.DIRT.getDefaultInstance(), DIAMOND_CHESTPLATE.getDefaultInstance(), damagedDiamondChestplate);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            importer.setFuzzyMode(true);
            importer.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            importer.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertContainerContains(pos.east(), Items.DIRT))
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
    public static void shouldImportWater(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (importer, pos, sequence) -> {
            // Arrange
            helper.setBlock(pos.east(), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

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
