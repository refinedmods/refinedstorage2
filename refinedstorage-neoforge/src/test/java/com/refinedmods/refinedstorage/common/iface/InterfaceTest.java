package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;

import java.util.List;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.assertInterfaceEmpty;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.interfaceContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.iface.InterfaceTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class InterfaceTest {
    private InterfaceTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldExportItem(final GameTestHelper helper) {
        preparePlot(helper, (iface, pos, sequence) -> {
            // Arrange
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
            }));

            // Act
            iface.setFuzzyMode(false);
            iface.setFilters(List.of(
                new ResourceAmount(asResource(DIRT), 10),
                new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
            ));

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenExecute(() -> {
                    iface.clearFilters();
                    iface.setFilters(List.of(
                        new ResourceAmount(asResource(DIRT), 5)
                    ));
                })
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenExecute(() -> iface.setFilters(List.of(
                    new ResourceAmount(asResource(DIRT), 7)
                )))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 7)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 3),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExportItemFuzzy(final GameTestHelper helper) {
        preparePlot(helper, (iface, pos, sequence) -> {
            // Arrange
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
            }));

            // Act
            iface.setFuzzyMode(true);
            iface.setFilters(List.of(
                new ResourceAmount(asResource(DIRT), 5),
                new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
            ));

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> {
                    iface.clearFilters();
                    iface.setFilters(List.of(
                        new ResourceAmount(asResource(DIRT), 10))
                    );
                })
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExportFluid(final GameTestHelper helper) {
        preparePlot(helper, (iface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            iface.setFilters(List.of(new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10)));

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> iface.setFilters(List.of(
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 5))
                ))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 5)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 5),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> iface.setFilters(List.of(
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 7))
                ))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 7)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 3),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldImportItem(final GameTestHelper helper) {
        preparePlot(helper, (iface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 5);
                insert(helper, network, STONE, 15);
            }));

            // Assert
            sequence
                .thenWaitUntil(assertInterfaceEmpty(helper, pos))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> iface.getExportedResources().insert(asResource(DIRT), 5, Action.EXECUTE))
                .thenWaitUntil(assertInterfaceEmpty(helper, pos))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldImportFluid(final GameTestHelper helper) {
        preparePlot(helper, (iface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 5);
                insert(helper, network, STONE, 15);
            }));

            // Assert
            sequence
                .thenWaitUntil(assertInterfaceEmpty(helper, pos))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 5),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> iface.getExportedResources().insert(
                    asResource(WATER), Platform.INSTANCE.getBucketAmount() * 5, Action.EXECUTE
                ))
                .thenWaitUntil(assertInterfaceEmpty(helper, pos))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }
}
