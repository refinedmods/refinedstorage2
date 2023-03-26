package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;
import com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class DetectorNetworkNodeTest {
    private static final long ENERGY_USAGE = 3;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE, longValue = ENERGY_USAGE)
    })
    DetectorNetworkNode sut;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        storageChannel.addSource(new InMemoryStorageImpl<>());
        sut.setAmountStrategy(new DetectorAmountStrategyImpl());
    }

    @Test
    void testWithoutNetwork() {
        // Act
        sut.setTemplate(new TypedTemplate<>("A", NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        sut.setNetwork(null);

        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @Test
    void testWithoutActiveness() {
        // Act
        sut.setTemplate(new TypedTemplate<>("A", NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        sut.setActive(false);

        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @Test
    void testWithoutTemplate() {
        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @ParameterizedTest
    @EnumSource(DetectorMode.class)
    void testWithTemplateButWithoutResourceInNetwork(final DetectorMode mode) {
        // Arrange
        sut.setTemplate(new TypedTemplate<>("A", NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        sut.setMode(mode);

        // Act
        final boolean activated = sut.isActivated();

        // Assert
        if (mode == DetectorMode.EQUAL) {
            assertThat(activated).isTrue();
        } else {
            assertThat(activated).isFalse();
        }
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(mode);
    }

    public static Stream<Arguments> testCases() {
        return Stream.of(
            Arguments.of(DetectorMode.EQUAL, 10, 0, false),
            Arguments.of(DetectorMode.EQUAL, 10, 9, false),
            Arguments.of(DetectorMode.EQUAL, 10, 10, true),
            Arguments.of(DetectorMode.EQUAL, 10, 11, false),

            Arguments.of(DetectorMode.ABOVE, 10, 0, false),
            Arguments.of(DetectorMode.ABOVE, 10, 10, false),
            Arguments.of(DetectorMode.ABOVE, 10, 11, true),
            Arguments.of(DetectorMode.ABOVE, 10, 12, true),

            Arguments.of(DetectorMode.UNDER, 10, 0, true),
            Arguments.of(DetectorMode.UNDER, 10, 9, true),
            Arguments.of(DetectorMode.UNDER, 10, 10, false),
            Arguments.of(DetectorMode.UNDER, 10, 11, false)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testModes(final DetectorMode mode,
                   final long comparisonAmount,
                   final long amountInNetwork,
                   final boolean expectedActivated,
                   @InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        sut.setTemplate(new TypedTemplate<>("A", NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        sut.setMode(mode);
        sut.setAmount(comparisonAmount);

        if (amountInNetwork > 0) {
            storageChannel.insert("A", amountInNetwork, Action.EXECUTE, EmptyActor.INSTANCE);
        }

        // Act
        final boolean activated = sut.isActivated();

        // Assert
        assertThat(activated).isEqualTo(expectedActivated);
        assertThat(sut.getAmount()).isEqualTo(comparisonAmount);
    }
}
