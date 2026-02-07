package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;

import java.util.Set;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkBlockEntityActiveness;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkEnergyInNetwork;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.networking.RelayTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;

public final class RelayTest {
    private RelayTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldPassThrough(final GameTestHelper helper) {
        preparePlot(helper, (relay, pos, subnetworkPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Assert
            sequence
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, subnetworkPos, true))
                .thenWaitUntil(checkEnergyInNetwork(helper, subnetworkPos, stored -> stored))
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
}
