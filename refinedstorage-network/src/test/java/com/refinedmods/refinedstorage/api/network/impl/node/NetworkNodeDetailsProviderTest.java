package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.network.test.RecordingNetworkNodeListener;
import com.refinedmods.refinedstorage.network.test.nodefactory.NetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.SimpleNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.StorageNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.StorageTransferNetworkNodeFactory;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NetworkNodeDetailsProviderTest {
    private static Stream<Fixture> fixtures() {
        return Stream.of(
            new Fixture(
                "SimpleNetworkNode",
                SimpleNetworkNodeFactory.TYPE,
                new SimpleNetworkNodeFactory(),
                (node, energyUsage) -> ((SimpleNetworkNode) node).setEnergyUsage(energyUsage)
            ),
            new Fixture(
                "StorageNetworkNode",
                StorageNetworkNodeFactory.TYPE,
                new StorageNetworkNodeFactory(),
                (node, energyUsage) -> ((AbstractStorageContainerNetworkNode) node).setBaseEnergyUsage(energyUsage)
            ),
            new Fixture(
                "StorageTransferNetworkNode",
                StorageTransferNetworkNodeFactory.TYPE,
                new StorageTransferNetworkNodeFactory(),
                (node, energyUsage) -> ((AbstractStorageContainerNetworkNode) node).setBaseEnergyUsage(energyUsage)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldExposeItsType(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);

        // Act & assert
        assertThat(fixture.asDetailsProvider(node).getType()).isEqualTo(fixture.type());
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldCreateDetailsForActiveNode(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, true);

        // Act
        final NetworkNodeDetails details = fixture.asDetailsProvider(node).createDetails();

        // Assert
        assertThat(details).usingRecursiveComparison().isEqualTo(new SimpleNetworkNodeDetails(3, true));
        assertThat(((SimpleNetworkNodeDetails) details).getEnergyUsage()).isEqualTo(3);
        assertThat(((SimpleNetworkNodeDetails) details).isActive()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldCreateDetailsForInactiveNode(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);

        // Act
        final NetworkNodeDetails details = fixture.asDetailsProvider(node).createDetails();

        // Assert
        assertThat(details).usingRecursiveComparison().isEqualTo(new SimpleNetworkNodeDetails(3, false));
        assertThat(((SimpleNetworkNodeDetails) details).getEnergyUsage()).isEqualTo(3);
        assertThat(((SimpleNetworkNodeDetails) details).isActive()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldNotifyListenerWhenEnergyUsageChanges(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        fixture.asDetailsProvider(node).addListener(listener);

        // Act
        fixture.changeEnergyUsage(node, 7);

        // Assert
        assertThat(node.getEnergyUsage()).isEqualTo(7);
        assertThat(listener.events).containsExactly(new NetworkNodeDetailsChangedEvent(7, false));
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldNotifyListenerWhenActiveChanges(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        fixture.asDetailsProvider(node).addListener(listener);

        // Act
        node.setActive(true);
        node.setActive(false);

        // Assert
        assertThat(listener.events).containsExactly(
            new NetworkNodeDetailsChangedEvent(3, true),
            new NetworkNodeDetailsChangedEvent(3, false)
        );
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldNotifyListenerWithLatestEnergyUsageWhenActiveChanges(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        fixture.changeEnergyUsage(node, 20);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        fixture.asDetailsProvider(node).addListener(listener);

        // Act
        node.setActive(true);

        // Assert
        assertThat(listener.events).containsExactly(new NetworkNodeDetailsChangedEvent(20, true));
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldNotifyMultipleListeners(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final NetworkNodeDetailsProvider detailsProvider = fixture.asDetailsProvider(node);
        final RecordingNetworkNodeListener listener1 = new RecordingNetworkNodeListener();
        final RecordingNetworkNodeListener listener2 = new RecordingNetworkNodeListener();
        detailsProvider.addListener(listener1);
        detailsProvider.addListener(listener2);

        // Act
        fixture.changeEnergyUsage(node, 11);

        // Assert
        assertThat(listener1.events).containsExactly(new NetworkNodeDetailsChangedEvent(11, false));
        assertThat(listener2.events).containsExactly(new NetworkNodeDetailsChangedEvent(11, false));
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldNotNotifyRemovedListener(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final NetworkNodeDetailsProvider detailsProvider = fixture.asDetailsProvider(node);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();
        detailsProvider.addListener(listener);
        detailsProvider.removeListener(listener);

        // Act
        fixture.changeEnergyUsage(node, 9);
        node.setActive(true);

        // Assert
        assertThat(listener.events).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldKeepNotifyingRemainingListenersAfterOneIsRemoved(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final NetworkNodeDetailsProvider detailsProvider = fixture.asDetailsProvider(node);
        final RecordingNetworkNodeListener listener1 = new RecordingNetworkNodeListener();
        final RecordingNetworkNodeListener listener2 = new RecordingNetworkNodeListener();
        detailsProvider.addListener(listener1);
        detailsProvider.addListener(listener2);
        detailsProvider.removeListener(listener1);

        // Act
        fixture.changeEnergyUsage(node, 13);

        // Assert
        assertThat(listener1.events).isEmpty();
        assertThat(listener2.events).containsExactly(new NetworkNodeDetailsChangedEvent(13, false));
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldIgnoreRemovalOfListenerThatIsNotListening(final Fixture fixture) {
        // Arrange
        final AbstractNetworkNode node = fixture.create(3, false);
        final NetworkNodeDetailsProvider detailsProvider = fixture.asDetailsProvider(node);
        final RecordingNetworkNodeListener listener = new RecordingNetworkNodeListener();

        // Act & assert
        assertDoesNotThrow(() -> detailsProvider.removeListener(listener));
    }

    private record Fixture(
        String label,
        NetworkNodeType type,
        NetworkNodeFactory factory,
        BiConsumer<AbstractNetworkNode, Long> energyUsageChanger
    ) {
        @Override
        public String toString() {
            return label;
        }

        AbstractNetworkNode create(final long energyUsage, final boolean active) {
            return (AbstractNetworkNode) factory.create(Map.of(
                PROPERTY_ENERGY_USAGE, energyUsage,
                PROPERTY_ACTIVE, active
            ));
        }

        NetworkNodeDetailsProvider asDetailsProvider(final AbstractNetworkNode node) {
            return (NetworkNodeDetailsProvider) node;
        }

        void changeEnergyUsage(final AbstractNetworkNode node, final long newEnergyUsage) {
            energyUsageChanger.accept(node, newEnergyUsage);
        }
    }
}
