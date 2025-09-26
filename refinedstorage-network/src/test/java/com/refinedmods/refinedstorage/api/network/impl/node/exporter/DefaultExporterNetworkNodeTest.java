package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.task.DefaultSchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.network.impl.node.exporter.MissingResourcesListeningExporterTransferStrategy.OnMissingResources.scheduleAutocrafting;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE2;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @AddNetworkNode
    PatternProviderNetworkNode patternProvider;

    @Override
    protected SchedulingMode createSchedulingMode() {
        return new DefaultSchedulingMode();
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new TrackedStorageImpl(new StorageImpl(), () -> 1L));
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 99),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(storage.findTrackedResourceByActorType(A, NetworkNodeActor.class))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource(ExporterNetworkNode.class.getName(), 1));
        assertThat(storage.findTrackedResourceByActorType(B, NetworkNodeActor.class)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotTransferIfTheAmountRequestedIsLessThanZero(final long amount) {
        // Arrange
        final ExporterTransferStrategy strategy = createTransferStrategy(new StorageImpl(), amount);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.SKIPPED);
    }

    @Test
    void shouldTransferFirstNeededResourceIfTransferQuotaCausesSkip(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = new ExporterTransferStrategyImpl(destination, resource ->
            resource == A ? 0 : 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.SKIPPED);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(B, 7, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.RESOURCE_MISSING);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAcceptedInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (A.equals(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 20);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B, C));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(C, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(C, 10)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(C, 10)
        );
    }

    @Test
    void shouldUseFirstAvailableExpandedResource(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = new ExporterTransferStrategyImpl(
            destination,
            resource -> 5,
            (rootStorage, resource) -> resource.equals(A)
                ? List.of(A, A_ALTERNATIVE)
                : List.of(resource)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_ALTERNATIVE, 5),
            new ResourceAmount(B, 10),
            new ResourceAmount(C, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_ALTERNATIVE, 5)
        );
    }

    @Test
    void shouldUseFirstAvailableExpandedResourceWhenExecutingExtractFails(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl() {
            @Override
            public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE && resource == A) {
                    return 0;
                }
                return super.extract(resource, amount, action, actor);
            }
        });
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        storage.insert(A_ALTERNATIVE, 5, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = new ExporterTransferStrategyImpl(
            destination,
            resource -> 5,
            (rootStorage, resource) -> resource.equals(A)
                ? List.of(A, A_ALTERNATIVE)
                : List.of(resource)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_ALTERNATIVE, 5)
        );
    }

    @Test
    void shouldReturnLeftoverToStorageWhenExecutingInsertPartiallyFails(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE && amount == 10 && resource.equals(A)) {
                    super.insert(A, 7, action, actor);
                    return 7;
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final ExporterTransferStrategy strategy = new ExporterTransferStrategyImpl(
            destination,
            resource -> 10,
            (rootStorage, resource) -> resource.equals(A)
                ? List.of(A, A_ALTERNATIVE)
                : List.of(resource)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 3),
            new ResourceAmount(A_ALTERNATIVE, 10),
            new ResourceAmount(B, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
    }

    @Test
    void shouldNotTryOtherExpandedResourcesWhenOneIsNotAccepted(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(A_ALTERNATIVE2, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (A_ALTERNATIVE.equals(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final ExporterTransferStrategy strategy = new ExporterTransferStrategyImpl(
            destination,
            resource -> 5,
            (rootStorage, resource) -> resource.equals(A)
                ? List.of(A, A_ALTERNATIVE, A_ALTERNATIVE2)
                : List.of(resource)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_ALTERNATIVE, 10),
            new ResourceAmount(A_ALTERNATIVE2, 10),
            new ResourceAmount(B, 5)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 5)
        );
    }

    @Test
    void shouldNotStartAutocraftingTaskForMissingResourcesIfItIsNotConfiguredToDoSo(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());
        patternProvider.setStepBehavior(new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 10;
            }
        });

        storage.addSource(new StorageImpl());
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new LimitedStorageImpl(100);
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();
        assertThat(autocrafting.getStatuses()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 90),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
    }

    @Test
    void shouldNotStartAutocraftingTaskForMissingResourcesIfThereIsNoPatternForTheMissingResource(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new LimitedStorageImpl(100);
        final ExporterTransferStrategy strategy = new MissingResourcesListeningExporterTransferStrategy(
            new ExporterTransferStrategyImpl(destination, resource -> 10),
            scheduleAutocrafting(resource -> 1)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.RESOURCE_MISSING);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 90)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
        assertThat(autocrafting.getStatuses()).isEmpty();
    }

    @Test
    void shouldNotStartAutocraftingTaskForMissingResourcesIfMissingResourceCannotBeCraftedDueToMissingIngredients(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new LimitedStorageImpl(100);
        final ExporterTransferStrategy strategy = new MissingResourcesListeningExporterTransferStrategy(
            new ExporterTransferStrategyImpl(destination, resource -> 10),
            scheduleAutocrafting(resource -> 1)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.AUTOCRAFTING_MISSING_RESOURCES);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 90)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
        assertThat(autocrafting.getStatuses()).isEmpty();
    }

    @Test
    void shouldNotStartAutocraftingTaskIfResourceIsAvailableButCannotBeInserted(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());

        storage.addSource(new StorageImpl());
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        final InsertableStorage destination = (resource, amount, action, actor) -> 0;
        final ExporterTransferStrategy strategy = new MissingResourcesListeningExporterTransferStrategy(
            new ExporterTransferStrategyImpl(destination, resource -> 10),
            scheduleAutocrafting(resource -> 1)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.DESTINATION_DOES_NOT_ACCEPT);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.DESTINATION_DOES_NOT_ACCEPT);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(autocrafting.getStatuses()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotStartAutocraftingTaskIfAmountIsLessThanZero(
        final long amount,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(B, 1).ingredient(A, 1).build());

        final Storage destination = new LimitedStorageImpl(100);
        final ExporterTransferStrategy strategy = new MissingResourcesListeningExporterTransferStrategy(
            new ExporterTransferStrategyImpl(destination, resource -> 10),
            scheduleAutocrafting(resource -> amount)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(B));

        // Act
        sut.doWork();

        // Assert
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.DESTINATION_DOES_NOT_ACCEPT);
        assertThat(autocrafting.getStatuses()).isEmpty();
    }

    @Test
    void shouldStartAutocraftingTaskForMissingResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        patternProvider.setPattern(1, pattern().output(A, 1).ingredient(C, 1).build());
        patternProvider.setStepBehavior(new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 10;
            }
        });

        storage.addSource(new StorageImpl());
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new LimitedStorageImpl(100);
        final ExporterTransferStrategy strategy = new MissingResourcesListeningExporterTransferStrategy(
            new ExporterTransferStrategyImpl(destination, resource -> 10),
            scheduleAutocrafting(resource -> 1)
        );

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(sut.getLastResult(1)).isNull();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(autocrafting.getStatuses()).isEmpty();

        sut.doWork();
        assertThat(sut.getLastResult(0)).isEqualTo(ExporterTransferStrategy.Result.AUTOCRAFTING_STARTED);
        assertThat(sut.getLastResult(1)).isEqualTo(ExporterTransferStrategy.Result.EXPORTED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 90),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 10)
        );
        assertThat(autocrafting.getStatuses()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 80),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 20)
        );
        assertThat(autocrafting.getStatuses()).hasSize(1);

        patternProvider.doWork();
        patternProvider.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 80),
            new ResourceAmount(C, 99),
            new ResourceAmount(A, 1)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 80),
            new ResourceAmount(C, 99)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 6),
            new ResourceAmount(B, 20)
        );
        assertThat(autocrafting.getStatuses()).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 70),
            new ResourceAmount(C, 99)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 6),
            new ResourceAmount(B, 30)
        );
        assertThat(autocrafting.getStatuses()).hasSize(1);
    }
}
