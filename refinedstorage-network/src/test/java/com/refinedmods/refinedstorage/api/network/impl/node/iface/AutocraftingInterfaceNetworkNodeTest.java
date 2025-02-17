package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class AutocraftingInterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode sut;

    @AddNetworkNode
    PatternProviderNetworkNode patternProvider;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        sut.setTransferQuotaProvider(resource -> 10);
        sut.setExportState(exportState);
    }

    @Test
    void shouldNotAutocraftMissingResourcesByDefault(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());

        exportState.setRequestedResource(0, A, 20);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedAmount(0)).isZero();
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.RESOURCE_MISSING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(patternProvider.getTasks()).isEmpty();
    }

    @Test
    void shouldNotAutocraftMissingResourcesWhenPatternIsMissing(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setOnMissingResources(new InterfaceNetworkNode.AutocraftOnMissingResources());

        storage.addSource(new StorageImpl());

        exportState.setRequestedResource(0, A, 20);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedAmount(0)).isZero();
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.RESOURCE_MISSING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(patternProvider.getTasks()).isEmpty();
    }

    @Test
    void shouldNotAutocraftMissingResourcesWhenIngredientsForPatternAreMissing(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setOnMissingResources(new InterfaceNetworkNode.AutocraftOnMissingResources());
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());

        exportState.setRequestedResource(0, A, 20);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedAmount(0)).isZero();
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.AUTOCRAFTING_MISSING_RESOURCES);
        assertThat(storage.getAll()).isEmpty();
        assertThat(patternProvider.getTasks()).isEmpty();
    }

    @Test
    void shouldAutocraftMissingResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setOnMissingResources(new InterfaceNetworkNode.AutocraftOnMissingResources());
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(0, A, 20);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedAmount(0)).isZero();
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.AUTOCRAFTING_STARTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 100)
        );
        assertThat(patternProvider.getTasks())
            .hasSize(1)
            .allMatch(t -> t.getAmount() == 10);
    }

    @Test
    void shouldAutocraftMissingResourcesWithLessIngredientsThanNecessary(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setOnMissingResources(new InterfaceNetworkNode.AutocraftOnMissingResources());
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());
        storage.insert(C, 5, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(0, A, 20);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedAmount(0)).isZero();
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.AUTOCRAFTING_STARTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 5)
        );
        assertThat(patternProvider.getTasks())
            .hasSize(1)
            .allMatch(t -> t.getAmount() == 5);
    }

    @Test
    void shouldAutocraftMoreMissingResourcesWhenAlreadyHavingInitialResource(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setOnMissingResources(new InterfaceNetworkNode.AutocraftOnMissingResources());
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(0, A, 30);
        exportState.setCurrentlyExported(0, A, 10);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(0)).isEqualTo(10);
        assertThat(sut.getLastResult(0)).isEqualTo(InterfaceTransferResult.AUTOCRAFTING_STARTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 100)
        );
        assertThat(patternProvider.getTasks())
            .hasSize(1)
            .allMatch(t -> t.getAmount() == 10);
    }
}
