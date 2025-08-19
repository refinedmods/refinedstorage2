package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import java.util.Collection;
import java.util.Collections;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.CRAFTING_TABLE_YIELD_2X_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.IRON_INGOT_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.IRON_PICKAXE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SMOOTH_STONE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SPRUCE_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.STICKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.STONE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.COBBLESTONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_INGOT;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_ORE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_PICKAXE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SMOOTH_STONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STONE_BRICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProviderImpl.sinkKey;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TaskImplTest {
    private static final RecursiveComparisonConfiguration STATUS_CONFIG = RecursiveComparisonConfiguration.builder()
        .withIgnoredFields("info.startTime")
        .build();
    private static final ExternalPatternSinkProvider EMPTY_SINK_PROVIDER = pattern -> Collections.emptyList();

    @Test
    void testInitialState() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);

        // Act
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);

        // Assert
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        assertThat(task.getId()).isNotNull();
        assertThat(task.getId()).hasToString(task.getId().id().toString());
        assertThat(task.shouldNotify()).isTrue();
        assertThat(task.getActor()).isEqualTo(Actor.EMPTY);
        assertThat(task.getResource()).isEqualTo(CRAFTING_TABLE);
        assertThat(task.getAmount()).isEqualTo(3);
    }

    @Test
    void shouldCancelTask() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, CRAFTING_TABLE, 3);

        // Act & assert
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_LOG, 1),
                new ResourceAmount(OAK_LOG, 1),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.shouldNotify()).isTrue();
        assertThat(task.getActor()).isEqualTo(Actor.EMPTY);

        task.cancel();

        assertThat(task.shouldNotify()).isFalse();
        assertThat(task.getActor()).isEqualTo(Actor.EMPTY);

        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_LOG, 1),
                new ResourceAmount(OAK_LOG, 1),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(storage.getAll()).isEmpty();

        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
    }

    @Test
    void shouldExtractAllResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);

        // Act
        final boolean changed = task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);

        // Assert
        assertThat(changed).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 1),
                new ResourceAmount(SPRUCE_LOG, 1),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );
    }

    @Test
    void shouldPartiallyExtractAllResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);
        storage.extract(OAK_PLANKS, 4, Action.EXECUTE, Actor.EMPTY);
        storage.extract(OAK_LOG, 1, Action.EXECUTE, Actor.EMPTY);
        storage.extract(SPRUCE_LOG, 1, Action.EXECUTE, Actor.EMPTY);

        // Act & assert
        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isFalse();
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_LOG, 1, Action.EXECUTE, Actor.EMPTY);
        storage.insert(SPRUCE_LOG, 1, Action.EXECUTE, Actor.EMPTY);

        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isFalse();
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 3, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(OAK_PLANKS, 1)
        );
    }

    @Test
    void shouldCompleteTaskWithInternalPatterns() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, CRAFTING_TABLE, 3);

        // Act & assert
        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(SIGN, 10)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 8)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 1)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 3),
                new ResourceAmount(OAK_PLANKS, 5)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 2)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 2),
                new ResourceAmount(OAK_PLANKS, 2)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(copyInternalStorage(task)).isEmpty();

        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }

    @Test
    void shouldPartiallyReturnOutputsWithInternalPatterns() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(
            OAK_PLANKS_PATTERN,
            SPRUCE_PLANKS_PATTERN,
            CRAFTING_TABLE_YIELD_2X_PATTERN
        );
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, CRAFTING_TABLE, 4);

        final RootStorage returnStorage = new RootStorageImpl();
        returnStorage.addSource(new LimitedStorageImpl(3));

        // Act & assert
        task.step(returnStorage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(returnStorage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 4)
            );

        task.step(returnStorage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(SPRUCE_PLANKS, 2)
            );

        assertThat(task.step(returnStorage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(CRAFTING_TABLE, 1)
            );

        assertThat(task.step(returnStorage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isFalse();
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(CRAFTING_TABLE, 1)
            );

        final RootStorage finalReturnStorage = new RootStorageImpl();
        finalReturnStorage.addSource(new StorageImpl());

        assertThat(
            task.step(finalReturnStorage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(finalReturnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 1)
        );
        assertThat(copyInternalStorage(task)).isEmpty();
    }

    @Test
    void shouldCompleteTaskWithExternalPatternAsChildPatternOfInternalPattern() {
        // Arrange
        final TaskListener listener = mock(TaskListener.class);
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2),
            new ResourceAmount(IRON_ORE, 3)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        final var ironOreSink = sinkProvider.put(IRON_INGOT_PATTERN);
        Task task = getRunningTask(storage, patterns, sinkProvider, IRON_PICKAXE, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3),
                new ResourceAmount(STICKS, 2)
            );

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 1)
        );
        verify(listener, never()).receivedExternalIteration(any());

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 2)
        );
        verify(listener, never()).receivedExternalIteration(any());

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        verify(listener, never()).receivedExternalIteration(any());

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        verify(listener, never()).receivedExternalIteration(any());

        storage.removeListener(task);
        task = new TaskImpl(((TaskImpl) task).createSnapshot());
        storage.addListener(task);

        storage.insert(IRON_INGOT, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 1),
                new ResourceAmount(STICKS, 2)
            );
        verify(listener, never()).receivedExternalIteration(any());

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 1),
                new ResourceAmount(STICKS, 2)
            );
        verify(listener, times(1)).receivedExternalIteration(IRON_INGOT_PATTERN);
        clearInvocations(listener);

        storage.insert(IRON_INGOT, 5, Action.EXECUTE, Actor.EMPTY);
        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_INGOT, 3),
            new ResourceAmount(STONE, 2)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 3),
                new ResourceAmount(STICKS, 2)
            );
        verify(listener, never()).receivedExternalIteration(any());

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, listener);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_PICKAXE, 1),
            new ResourceAmount(IRON_INGOT, 3),
            new ResourceAmount(STONE, 2)
        );
        assertThat(copyInternalStorage(task)).isEmpty();
        verify(listener, times(1)).receivedExternalIteration(IRON_INGOT_PATTERN);
        clearInvocations(listener);
    }

    @Test
    void shouldCompleteTaskWithExternalPatternsThatAreDependentOnEachOther() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(COBBLESTONE, 64)
        );
        final PatternRepository patterns = patterns(STONE_PATTERN, SMOOTH_STONE_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl cobblestoneSink = sinkProvider.put(STONE_PATTERN);
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl stoneSink =
            sinkProvider.put(SMOOTH_STONE_PATTERN);
        final Task task = getRunningTask(storage, patterns, sinkProvider, SMOOTH_STONE, 4);

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 60)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 4)
            );

        // Act & assert
        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 3)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 2)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 2),
                new ResourceAmount(STONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 2)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 1),
                new ResourceAmount(STONE, 1)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 3)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 1)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isFalse();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(STONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 1)
        );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 3)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );

        storage.insert(SMOOTH_STONE, 4, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(COBBLESTONE, 60),
            new ResourceAmount(SMOOTH_STONE, 4)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(COBBLESTONE, 60),
            new ResourceAmount(SMOOTH_STONE, 4)
        );

        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isFalse();
    }

    @Test
    void shouldCompleteTaskWithExternalPatternsThatShareTheSameOutputResources() {
        // Arrange
        final Pattern aToStonePattern = pattern(PatternType.EXTERNAL)
            .ingredient(A, 1)
            .output(STONE, 1)
            .build();
        final Pattern stoneBricksPattern = pattern()
            .ingredient(STONE, 1)
            .ingredient(STONE, 1)
            .output(STONE_BRICKS, 1)
            .build();
        final RootStorage storage = storage(
            new ResourceAmount(COBBLESTONE, 1),
            new ResourceAmount(A, 1)
        );
        final PatternRepository patterns = patterns(STONE_PATTERN, aToStonePattern, stoneBricksPattern);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl cobblestoneSink = sinkProvider.put(STONE_PATTERN);
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl aSink = sinkProvider.put(aToStonePattern);
        final Task task = getRunningTask(storage, patterns, sinkProvider, STONE_BRICKS, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 1),
                new ResourceAmount(A, 1)
            );

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(aSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(aSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
    }

    @Test
    void shouldNotCompleteTaskWithExternalPatternIfSinkDoesNotAcceptResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2 * 2),
            new ResourceAmount(IRON_ORE, 3 * 2)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        sinkProvider.put(IRON_INGOT_PATTERN, ExternalPatternSink.Result.REJECTED);
        final Task task = getRunningTask(storage, patterns, sinkProvider, IRON_PICKAXE, 2);

        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3 * 2),
                new ResourceAmount(STICKS, 2 * 2)
            );

        // Act & assert
        for (int i = 0; i < 6; ++i) {
            task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
            assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
            assertThat(copyInternalStorage(task))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                    new ResourceAmount(IRON_ORE, 3 * 2),
                    new ResourceAmount(STICKS, 2 * 2)
                );
        }

        final var ironOreSink = sinkProvider.put(IRON_INGOT_PATTERN);
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, (3 * 2) - 1),
                new ResourceAmount(STICKS, 2 * 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_ORE, 1)
        );
    }

    @Test
    void shouldNotCompleteTaskWithExternalPatternIfSinkDoesNotAcceptResourcesOnlyWhenExecuting() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2),
            new ResourceAmount(IRON_ORE, 3)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        sinkProvider.put(IRON_INGOT_PATTERN, (pattern, resources, action) -> action == Action.EXECUTE
            ? ExternalPatternSink.Result.REJECTED
            : ExternalPatternSink.Result.ACCEPTED);
        final Task task = getRunningTask(storage, patterns, sinkProvider, IRON_PICKAXE, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3),
                new ResourceAmount(STICKS, 2)
            );

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2), // we have voided 1 iron ore
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1), // we have voided 1 iron ore
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(STICKS, 2));

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(STICKS, 2));
    }

    @Test
    void shouldRespectPatternInputOrderForExternalPattern() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(COBBLESTONE, 64),
            new ResourceAmount(IRON_INGOT, 64),
            new ResourceAmount(STICKS, 64)
        );
        final Pattern pattern = pattern(PatternType.EXTERNAL)
            .ingredient(COBBLESTONE, 2)
            .ingredient(IRON_INGOT, 3)
            .ingredient(STICKS, 1)
            .output(STONE, 1)
            .build();
        final PatternRepository patterns = patterns(pattern);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl sink = sinkProvider.put(pattern);
        final Task task = getRunningTask(storage, patterns, sinkProvider, STONE, 1);

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(COBBLESTONE, 62),
            new ResourceAmount(IRON_INGOT, 61),
            new ResourceAmount(STICKS, 63)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 2),
                new ResourceAmount(IRON_INGOT, 3),
                new ResourceAmount(STICKS, 1)
            );

        // Act & assert
        assertThat(task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY)).isTrue();
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 2),
            new ResourceAmount(IRON_INGOT, 3),
            new ResourceAmount(STICKS, 1)
        );
    }

    @Test
    void shouldStepAccordingToCustomStepBehavior() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, CRAFTING_TABLE, 10);

        // Act & assert
        task.step(storage, EMPTY_SINK_PROVIDER, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 3 : 1;
            }
        }, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 10 - 3),
                new ResourceAmount(OAK_PLANKS, 4 * 3)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 4 : (pattern == CRAFTING_TABLE_PATTERN ? 10 : 1);
            }
        }, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 10 - 3 - 4),
                new ResourceAmount(OAK_PLANKS, 4 * 4)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, new StepBehavior() {
            @Override
            public boolean canStep(final Pattern pattern) {
                return pattern != CRAFTING_TABLE_PATTERN;
            }

            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 999 : 1;
            }
        }, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 4 * 7)
            );

        task.step(storage, EMPTY_SINK_PROVIDER, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 999;
            }
        }, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 10)
        );
        assertThat(copyInternalStorage(task)).isEmpty();
    }

    @Test
    void shouldReportStatusCorrectlyForInternalPatterns() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(OAK_LOG, 10));
        final PatternRepository patterns = patterns(
            OAK_PLANKS_PATTERN,
            CRAFTING_TABLE_PATTERN
        );
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, CRAFTING_TABLE, 2);

        // Act & assert
        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), CRAFTING_TABLE, 2, 0)
                .crafting(CRAFTING_TABLE, 2)
                .crafting(OAK_PLANKS, 4)
                .stored(OAK_PLANKS, 4)
                .stored(OAK_LOG, 1)
                .build(0.25));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 1),
                new ResourceAmount(OAK_PLANKS, 4)
            );
    }

    @Test
    void shouldReportStatusCorrectlyForInternalAndExternalPatterns() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 10),
            new ResourceAmount(IRON_ORE, 10)
        );
        final PatternRepository patterns = patterns(
            OAK_PLANKS_PATTERN,
            STICKS_PATTERN,
            IRON_INGOT_PATTERN,
            IRON_PICKAXE_PATTERN
        );
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        final ExternalPatternSinkProviderImpl.ExternalPatternSinkImpl ironOreSink =
            sinkProvider.put(IRON_INGOT_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, IRON_PICKAXE, 1);

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .scheduled(IRON_INGOT, 2)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .crafting(STICKS, 4)
                .stored(OAK_PLANKS, 4)
                .stored(IRON_ORE, 2)
                .build(0.2));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 1)
        );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .scheduled(IRON_INGOT, 1)
                .processing(IRON_ORE, 2, sinkKey(IRON_INGOT_PATTERN))
                .stored(OAK_PLANKS, 2)
                .stored(IRON_ORE, 1)
                .stored(STICKS, 4)
                .build(0.5));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1),
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 2)
        );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .processing(IRON_ORE, 3, sinkKey(IRON_INGOT_PATTERN))
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .build(0.6666666666666666));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        storage.insert(IRON_INGOT, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .stored(IRON_INGOT, 2)
                .build(0.6666666666666666));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4),
                new ResourceAmount(IRON_INGOT, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        storage.insert(IRON_INGOT, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .stored(IRON_INGOT, 3)
                .build(0.6666666666666666));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4),
                new ResourceAmount(IRON_INGOT, 3)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7)
        );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 2)
                .build(1.0));
        assertThat(copyInternalStorage(task))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7),
            new ResourceAmount(IRON_PICKAXE, 1)
        );

        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0).build(1.0)
        );
        assertThat(copyInternalStorage(task)).isEmpty();
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7),
            new ResourceAmount(IRON_PICKAXE, 1),
            new ResourceAmount(OAK_PLANKS, 2),
            new ResourceAmount(STICKS, 2)
        );
    }

    @Test
    void shouldReportWhetherSinkIsRejectingInputsOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        sinkProvider.put(IRON_INGOT_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .build(0));

        sinkProvider.put(IRON_INGOT_PATTERN, ExternalPatternSink.Result.REJECTED);
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .rejected(IRON_INGOT)
                .build(0));
    }

    @Test
    void shouldReportWhetherSinkIsNotFoundOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        sinkProvider.put(IRON_INGOT_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .build(0));

        sinkProvider.remove(IRON_INGOT_PATTERN);
        task.step(storage, EMPTY_SINK_PROVIDER, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .noneFound(IRON_INGOT)
                .build(0));
    }

    @Test
    void shouldReportWhetherSinkIsLockedFoundOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternSinkProviderImpl sinkProvider = new ExternalPatternSinkProviderImpl();
        sinkProvider.put(IRON_INGOT_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK_PROVIDER, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .build(0));

        sinkProvider.put(IRON_INGOT_PATTERN, ExternalPatternSink.Result.LOCKED);
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TestTaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1, sinkKey(IRON_INGOT_PATTERN))
                .locked(IRON_INGOT)
                .build(0));
    }

    private static Task getTask(final RootStorage storage,
                                final PatternRepository patterns,
                                final ResourceKey resource,
                                final long amount) {
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);
        final Task task = calculatePlan(sut, resource, amount, CancellationToken.NONE).map(plan -> new TaskImpl(
            plan,
            MutableResourceListImpl.orderPreserving(),
            Actor.EMPTY,
            true
        )).orElseThrow();
        storage.addListener(task);
        return task;
    }

    private static Task getRunningTask(final RootStorage storage,
                                       final PatternRepository patterns,
                                       final ExternalPatternSinkProvider sinkProvider,
                                       final ResourceKey resource,
                                       final long amount) {
        final Task task = getTask(storage, patterns, resource, amount);
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        task.step(storage, sinkProvider, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        return task;
    }

    private static Collection<ResourceAmount> copyInternalStorage(final Task task) {
        return ((TaskImpl) task).createSnapshot().copyInternalStorage().copyState();
    }
}
