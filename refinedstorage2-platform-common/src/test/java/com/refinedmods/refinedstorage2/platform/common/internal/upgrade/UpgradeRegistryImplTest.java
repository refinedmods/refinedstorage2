package com.refinedmods.refinedstorage2.platform.common.internal.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeInDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SetupMinecraft
class UpgradeRegistryImplTest {
    UpgradeRegistry sut;

    @BeforeEach
    void setUp() {
        sut = new UpgradeRegistryImpl();
    }

    @Test
    void testAddingSingleApplicableUpgrade() {
        // Arrange
        final UpgradeDestination dest = new UpgradeDestinationImpl(null);

        // Act
        sut.addApplicableUpgrade(dest, () -> Items.DIRT, 1);

        // Assert
        assertThat(sut.getApplicableUpgrades(dest))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ApplicableUpgrade(() -> Items.DIRT, 1)
            );
    }

    @Test
    void testAddingDifferentApplicableUpgradesToTheSameDestination() {
        // Arrange
        final UpgradeDestination dest = new UpgradeDestinationImpl(null);

        // Act
        sut.addApplicableUpgrade(dest, () -> Items.DIRT, 1);
        sut.addApplicableUpgrade(dest, () -> Items.GLASS, 5);

        // Assert
        assertThat(sut.getApplicableUpgrades(dest))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ApplicableUpgrade(() -> Items.DIRT, 1),
                new ApplicableUpgrade(() -> Items.GLASS, 5)
            );
    }

    @Test
    void testRetrievingApplicableUpgradesFromUnknownDestination() {
        // Arrange
        final UpgradeDestination dest = new UpgradeDestinationImpl(null);

        // Assert
        assertThat(sut.getApplicableUpgrades(dest)).isEmpty();
    }

    @Test
    void testRetrievingSingleDestinationForItem() {
        // Arrange
        final UpgradeDestination dest = new UpgradeDestinationImpl(null);

        // Act
        sut.addApplicableUpgrade(dest, () -> Items.DIRT, 1);

        // Assert
        assertThat(sut.getDestinations(Items.DIRT))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new UpgradeInDestination(dest, 1)
            );
    }

    @Test
    void testRetrievingMultipleDestinationsForItem() {
        // Arrange
        final UpgradeDestination dest1 = new UpgradeDestinationImpl(Component.literal("A"));
        final UpgradeDestination dest2 = new UpgradeDestinationImpl(Component.literal("B"));

        // Act
        sut.addApplicableUpgrade(dest1, () -> Items.DIRT, 1);
        sut.addApplicableUpgrade(dest2, () -> Items.DIRT, 2);

        // Assert
        assertThat(sut.getDestinations(Items.DIRT))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new UpgradeInDestination(dest1, 1),
                new UpgradeInDestination(dest2, 2)
            );
    }

    @Test
    void testRetrievingMultipleDestinationsForDifferentItems() {
        // Arrange
        final UpgradeDestination dest1 = new UpgradeDestinationImpl(Component.literal("A"));
        final UpgradeDestination dest2 = new UpgradeDestinationImpl(Component.literal("B"));

        // Act
        sut.addApplicableUpgrade(dest1, () -> Items.DIRT, 1);
        sut.addApplicableUpgrade(dest2, () -> Items.DIRT, 2);
        sut.addApplicableUpgrade(dest2, () -> Items.GLASS, 3);

        // Assert
        assertThat(sut.getDestinations(Items.DIRT))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new UpgradeInDestination(dest1, 1),
                new UpgradeInDestination(dest2, 2)
            );

        assertThat(sut.getDestinations(Items.GLASS))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new UpgradeInDestination(dest2, 3)
            );
    }

    @Test
    void testRetrievingDestinationForUnknownItem() {
        // Arrange
        final UpgradeDestination dest = new UpgradeDestinationImpl(null);

        // Act
        sut.addApplicableUpgrade(dest, () -> Items.DIRT, 1);

        // Assert
        assertThat(sut.getDestinations(Items.GLASS)).isEmpty();
    }

    private record UpgradeDestinationImpl(Component name) implements UpgradeDestination {
        @Override
        public Component getName() {
            return name();
        }
    }
}
