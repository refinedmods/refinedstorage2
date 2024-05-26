package com.refinedmods.refinedstorage2.platform.common.exporter;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.refinedmods.refinedstorage2.platform.common.exporter.ExporterTestPlots.preparePlot;
import static com.refinedmods.refinedstorage2.platform.forge.GameTestUtil.*;
import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.level.material.Fluids.WATER;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ExporterTest {
    private ExporterTest() {
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExportBlock(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (exporter, pos, sequence) -> {
            // Arrange
            helper.setBlock(pos.east(), Blocks.CHEST);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            exporter.setFilters(List.of(asResource(DIRT)));

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertContainerContains(pos.east(), Blocks.DIRT.asItem()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 9),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExportBlockFuzzy(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (exporter, pos, sequence) -> {
            // Arrange
            helper.setBlock(pos.east(), Blocks.CHEST);

            ItemStack damagedDiamondChestplate = DIAMOND_CHESTPLATE.getDefaultInstance();
            damagedDiamondChestplate.setDamageValue(500);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
                insert(helper, network, new ItemResource(damagedDiamondChestplate.getItem(), damagedDiamondChestplate.getTag()), 1);
            }));

            // Act
            exporter.setFuzzyMode(true);
            exporter.setFilters(List.of(asResource(DIAMOND_CHESTPLATE)));

            // Assert
            sequence
                    .thenWaitUntil(() -> assertContainerContains(helper, pos.east(), DIAMOND_CHESTPLATE.getDefaultInstance()))
                    .thenWaitUntil(() -> assertContainerContains(helper, pos.east(), damagedDiamondChestplate))
                    .thenWaitUntil(storageContainsExactly(
                            helper,
                            pos,
                            new ResourceAmount(asResource(DIRT), 10),
                            new ResourceAmount(asResource(STONE), 15)
                    ))
                    .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExportWater(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (exporter, pos, sequence) -> {
            // Arrange
            helper.setBlock(pos.east(), Blocks.CAULDRON);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2);
            }));

            // Act
            exporter.setFilters(List.of(asResource(WATER)));

            // Assert
            sequence
                    .thenWaitUntil(() -> helper.assertBlockProperty(pos.east(), LayeredCauldronBlock.LEVEL, 3))
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
