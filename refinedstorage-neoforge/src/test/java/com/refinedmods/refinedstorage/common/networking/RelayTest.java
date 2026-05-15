package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterBlockEntity;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkBlockEntityActiveness;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkEnergyInNetwork;
import static com.refinedmods.refinedstorage.common.GameTestUtil.createCraftingPattern;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.hasAutocraftingPattern;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.startAutocraftingTask;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageIsEmpty;
import static com.refinedmods.refinedstorage.common.networking.RelayTestPlots.preparePlot;
import static net.minecraft.world.item.Items.CRAFTING_TABLE;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.OAK_LOG;
import static net.minecraft.world.item.Items.OAK_PLANKS;
import static net.minecraft.world.item.Items.STONE;

public final class RelayTest {
    private RelayTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughAll(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, OAK_LOG, 10);
                insert(helper, network, STONE, 15);
            }));
            helper.setBlock(subnetworkPos.above(), MOD_BLOCKS.getAutocrafter().getDefault().rotated(Direction.UP));

            // Act
            final BlockPos autocrafterPos1 = pos.below().west();
            helper.getBlockEntity(autocrafterPos1, AutocrafterBlockEntity.class).getPatternContainer().setItem(0,
                createCraftingPattern(List.of(OAK_PLANKS, OAK_PLANKS, OAK_PLANKS, OAK_PLANKS), List.of(0, 1, 3, 4)));
            final BlockPos autocrafterPos2 = subnetworkPos.above();
            helper.getBlockEntity(autocrafterPos2, AutocrafterBlockEntity.class).getPatternContainer().setItem(0,
                createCraftingPattern(List.of(OAK_LOG), List.of(0)));

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(checkEnergyInNetwork(helper, subnetworkPos, stored -> stored))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(OAK_LOG), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos1, asResource(OAK_PLANKS)))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos2, asResource(OAK_PLANKS)))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos1, asResource(CRAFTING_TABLE)))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos2, asResource(CRAFTING_TABLE)))
                .thenExecute(startAutocraftingTask(helper, autocrafterPos2,
                    new ResourceAmount(asResource(OAK_PLANKS), 4)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(OAK_PLANKS), 4),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(OAK_PLANKS), 4),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(startAutocraftingTask(helper, autocrafterPos1,
                    new ResourceAmount(asResource(CRAFTING_TABLE), 1)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(CRAFTING_TABLE), 1),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(CRAFTING_TABLE), 1),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldNotPassThrough(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, false))
                .thenWaitUntil(checkEnergyInNetwork(helper, subnetworkPos, stored -> 0L))
                .thenWaitUntil(storageContainsExactly(helper, subnetworkPos))
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
    public static void shouldPassThroughEnergy(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(checkEnergyInNetwork(helper, subnetworkPos, stored -> stored))
                .thenWaitUntil(storageContainsExactly(helper, subnetworkPos))
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
    public static void shouldPassThroughStorage(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
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
    public static void shouldPassThroughStorageBlocklist(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setFilters(Set.of(asResource(DIRT)));

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(STONE), 15)
                ))
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
    public static void shouldPassThroughStorageFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setFuzzyMode(true);
            relay.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            relay.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughStorageAllowlist(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setFilters(Set.of(asResource(DIRT)));
            relay.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 10)
                ))
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
    public static void shouldPassThroughStorageFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setFuzzyMode(true);
            relay.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            relay.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughStorageAndInsertExtract(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setAccessMode(AccessMode.INSERT_EXTRACT);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    insert(helper, network, DIRT, 10)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    extract(helper, network, DIRT, 10)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
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
    public static void shouldPassThroughStorageAndInsert(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setAccessMode(AccessMode.INSERT);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    insert(helper, network, DIRT, 10)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    extract(helper, network, DIRT, 10, false)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 20),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughStorageAndExtract(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassStorage(true);
            relay.setAccessMode(AccessMode.EXTRACT);

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    insert(helper, network, DIRT, 10, false)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network ->
                    extract(helper, network, DIRT, 10)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughAutocraftingRootNetwork(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, OAK_LOG, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassAutocrafting(true);

            final BlockPos autocrafterPos = pos.below().west();
            helper.getBlockEntity(autocrafterPos, AutocrafterBlockEntity.class).getPatternContainer().setItem(0,
                createCraftingPattern(List.of(OAK_LOG), List.of(0)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageIsEmpty(helper, subnetworkPos))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos, asResource(OAK_PLANKS)))
                .thenExecute(startAutocraftingTask(helper, autocrafterPos,
                    new ResourceAmount(asResource(OAK_PLANKS), 4)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 9),
                    new ResourceAmount(asResource(OAK_PLANKS), 4),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageIsEmpty(helper, subnetworkPos))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPassThroughAutocraftingSubNetwork(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, OAK_LOG, 10);
                insert(helper, network, STONE, 15);
            }));
            helper.setBlock(subnetworkPos.west(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
            sequence.thenWaitUntil(networkIsAvailable(helper, subnetworkPos, network -> {
                insert(helper, network, OAK_LOG, 1);
            }));

            // Act
            relay.setPassThrough(false);
            relay.setPassEnergy(true);
            relay.setPassAutocrafting(true);

            final BlockPos autocrafterPos = pos.below().west();
            helper.getBlockEntity(autocrafterPos, AutocrafterBlockEntity.class).getPatternContainer().setItem(0,
                createCraftingPattern(List.of(OAK_LOG), List.of(0)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(OAK_LOG), 1)
                ))
                .thenExecute(hasAutocraftingPattern(helper, autocrafterPos, asResource(OAK_PLANKS)))
                .thenExecute(startAutocraftingTask(helper, subnetworkPos,
                    new ResourceAmount(asResource(OAK_PLANKS), 4)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    subnetworkPos,
                    new ResourceAmount(asResource(OAK_PLANKS), 4)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(OAK_LOG), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }
}
