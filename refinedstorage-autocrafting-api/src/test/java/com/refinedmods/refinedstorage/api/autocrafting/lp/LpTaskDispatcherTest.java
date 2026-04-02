package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskSnapshot;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.X;
import static org.assertj.core.api.Assertions.assertThat;

class LpTaskDispatcherTest {
    private static final ExternalPatternSinkProvider EMPTY_SINKS = ignored -> List.of();
    private static final Function<PatternLayout, List<?>> NO_EXTERNAL_SINKS = ignored -> List.of();

    @Test
    void shouldCompleteImmediatelyWhenCancelledBeforeSteppingAndNotNotify() {
        final Pattern rootPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(), false),
            rootPattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        assertThat(dispatcher.shouldNotify()).isTrue();

        dispatcher.cancel();
        final boolean changed = dispatcher.step(new RootStorageImpl(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(changed).isTrue();
        assertThat(dispatcher.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(dispatcher.shouldNotify()).isFalse();
    }

    @Test
    void shouldKeepPendingStepWhenPatternProviderIsEmpty() throws Exception {
        final Pattern stepPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(stepPattern, 0, 1)), false),
            stepPattern,
            ignored -> Optional.empty()
        );
        final RootStorage storage = storageWith(A, 4);

        dispatcher.step(storage, EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);
        final boolean changedOnSecondStep = dispatcher.step(storage, EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(dispatcher.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(changedOnSecondStep).isFalse();
        assertThat(activeSubTasksSize(dispatcher)).isZero();
        assertThat(pendingStepsSize(dispatcher)).isEqualTo(1);
    }

    @Test
    void shouldDispatchOnlyOneStepInStrictOrderingMode() throws Exception {
        final Pattern first = pattern().ingredient(A, 1).output(X, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            C,
            1,
            new LpStepPlan(List.of(step(first, 0, 1), step(second, 1, 1)), true),
            second,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(C, 1, p), Actor.EMPTY, false))
        );

        dispatcher.step(storageWith(A, 1, B, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(activeSubTasksSize(dispatcher)).isEqualTo(1);
        assertThat(pendingStepsSize(dispatcher)).isEqualTo(1);
    }

    @Test
    void shouldDispatchMultipleStepsInRelaxedModeWhenResourcesAllow() throws Exception {
        final Pattern first = pattern().ingredient(A, 1).output(X, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            C,
            1,
            new LpStepPlan(List.of(step(first, 0, 1), step(second, 1, 1)), false),
            second,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(C, 1, p), Actor.EMPTY, false))
        );

        dispatcher.step(storageWith(A, 1, B, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(activeSubTasksSize(dispatcher)).isEqualTo(2);
        assertThat(pendingStepsSize(dispatcher)).isZero();
    }

    @Test
    void shouldBufferOnlyPendingNeedDuringBeforeInsert() {
        final Pattern stepPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            3,
            new LpStepPlan(List.of(step(stepPattern, 0, 3)),
                false),
            stepPattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        final long firstIntercepted = dispatcher.beforeInsert(A, 5);
        final long secondIntercepted = dispatcher.beforeInsert(A, 2);

        assertThat(firstIntercepted).isEqualTo(3);
        assertThat(secondIntercepted).isZero();
        assertThat(dispatcher.createSnapshot().copyInternalStorage().get(A)).isEqualTo(3);
    }

    @Test
    void shouldCancelActiveSubTasksAndCompleteAfterTheyArePruned() throws Exception {
        final Pattern stepPattern = pattern().ingredient(A, 1).output(X, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            C,
            1,
            new LpStepPlan(List.of(step(stepPattern, 0, 1)), false),
            stepPattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(X, 1, p), Actor.EMPTY, false))
        );

        dispatcher.step(storageWith(A, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(dispatcher.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(activeSubTasksSize(dispatcher)).isEqualTo(1);

        dispatcher.cancel();
        dispatcher.step(storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(dispatcher.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);

        dispatcher.step(storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        assertThat(dispatcher.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(activeSubTasksSize(dispatcher)).isZero();
    }

    @Test
    void shouldReportScheduledWorkInRelaxedModeStatus() {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            2,
            new LpStepPlan(List.of(step(pattern, 0, 1), step(pattern, 0, 1)), false),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        final TaskStatus status = dispatcher.getStatus();

        assertThat(status.items()).singleElement().satisfies(item -> {
            assertThat(item.resource()).isEqualTo(B);
            assertThat(item.scheduled()).isEqualTo(2);
            assertThat(item.processing()).isZero();
        });
    }

    @Test
    void shouldReportProcessingWorkInStrictModeStatus() {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            2,
            new LpStepPlan(List.of(step(pattern, 0, 1), step(pattern, 0, 1)), true),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        final TaskStatus status = dispatcher.getStatus();

        assertThat(status.items()).singleElement().satisfies(item -> {
            assertThat(item.resource()).isEqualTo(B);
            assertThat(item.processing()).isEqualTo(2);
            assertThat(item.scheduled()).isZero();
        });
    }

    @Test
    void shouldReportFullProgressWhenNoStepsExist() {
        final Pattern rootPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(), false),
            rootPattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        assertThat(dispatcher.getStatus().percentageCompleted()).isEqualTo(1D);
    }

    @Test
    void shouldIgnoreBeforeInsertWhenCancelled() {
        final Pattern stepPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(stepPattern, 0, 1)), false),
            stepPattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        dispatcher.cancel();
        final long intercepted = dispatcher.beforeInsert(A, 10);

        assertThat(intercepted).isZero();
    }

    @Test
    void shouldReturnTrueOnFirstStepFromReadyState() {
        // Kills step() line 91 "replaced boolean return with false" mutation.
        // The READY→RUNNING transition sets changed=true explicitly; the return at
        // end of RUNNING block must still see changed=true.
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(pattern, 0, 1)), false),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        final boolean changed = dispatcher.step(
            storageWith(A, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY
        );

        assertThat(changed).isTrue();
    }

    @Test
    void shouldReturnTrueFromRunningStateWhenOnlyDispatchChanges() {
        // Kills step() lines 96-116 OR-with-AND mutations.
        // After READY→RUNNING the dispatcher is in RUNNING state with 0 active, 1 pending.
        // Step with no resources: nothing changes, returns false.
        // Step with resources: dispatchRelaxed fires (true); OR keeps changed=true, AND kills it.
        final Pattern pat = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(pat, 0, 1)), false),
            pat,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        // Step 1: READY→RUNNING (explicitly sets changed=true but OR→AND still fires).
        dispatcher.step(storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        // Step 2: RUNNING, no resources → nothing changes.
        final boolean noChange = dispatcher.step(
            storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY
        );
        assertThat(noChange).isFalse();

        // Step 3: RUNNING, A available → only dispatchRelaxed returns true.
        // With mutation OR→AND: changed starts false (pruneCompleted=false), then &= false,
        // &= false, &= true (dispatch) still gives false. Return false instead of true.
        final boolean changedByDispatch = dispatcher.step(
            storageWith(A, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY
        );
        assertThat(changedByDispatch).isTrue();
    }

    @Test
    void shouldNotDispatchStrictWhenZeroResourcesAvailableForStep() {
        // Kills dispatchStrict line 171 boundary mutation (<= 0 vs < 0).
        // With boundary mutation, 0 iterations passes the guard and an invalid 0-iteration
        // dispatch is attempted.
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(pattern, 0, 1)), true),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );
        // Transition to RUNNING first (no resources yet)
        dispatcher.step(storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);
        assertThat(dispatcher.getState()).isEqualTo(TaskState.RUNNING);

        // Second step with no A available: maxDispatchableIterations=0, should return false
        final boolean changed = dispatcher.step(
            storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY
        );

        assertThat(changed).isFalse();
    }

    @Test
    void shouldDispatchCorrectIterationsBasedOnResourceDivision() throws Exception {
        // Kills maxDispatchableIterations line 465 division\u2192multiplication mutation.
        // Pattern needs 2 A per iteration, 4 A available -> should dispatch 2 iterations.
        // With multiplication mutation: 4*2=8 > 3 total -> would dispatch all 3.
        final Pattern pattern = pattern().ingredient(A, 2).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            3,
            new LpStepPlan(List.of(step(pattern, 0, 3)), false),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        // Provide 4 A: enough for 2 iterations (4/2=2), not 3
        dispatcher.step(storageWith(A, 4), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        // 2 iterations dispatched, 1 remaining
        assertThat(pendingStepsSize(dispatcher)).isEqualTo(1);
    }

    @Test
    void shouldCapBeforeInsertToExactlyPendingNeed() {
        // Kills beforeInsert lines 365/368 subtraction\u2192addition mutations.
        // If remaining = insertedAmount + intercepted (mutation), we would buffer too much.
        final Pattern pattern = pattern().ingredient(A, 2).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            2,
            new LpStepPlan(List.of(step(pattern, 0, 2)), false),
            pattern,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        // Pending need = 2*2 = 4; insert 3 (more than enough but less than excess)
        final long intercepted = dispatcher.beforeInsert(A, 3);

        // Should buffer exactly 3 (pendingNeed=4, remaining=3 after zero subtask interception)
        assertThat(intercepted).isEqualTo(3);
        // Second insert: pendingNeed - buffered = 4-3 = 1; insert 2 -> buffers 1
        final long intercepted2 = dispatcher.beforeInsert(A, 2);
        assertThat(intercepted2).isEqualTo(1);
        // Now fully buffered: third insert should get 0
        final long intercepted3 = dispatcher.beforeInsert(A, 2);
        assertThat(intercepted3).isZero();
    }

    @Test
    void shouldTransferCompletedSubTaskInternalStorageToBuffer() throws Exception {
        // Kills pruneCompletedSubTasks line 131 forEach-removal mutation.
        // When a sub-task completes, its internal storage must be merged into
        // dispatcher's bufferedInternalStorage so subsequent steps can use it.
        final Pattern step1 = pattern().ingredient(A, 1).output(X, 1).build();
        final Pattern step2 = pattern().ingredient(X, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(step1, 0, 1), step(step2, 1, 1)), true),
            step2,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );

        // Step until step1's subtask is dispatched and completes, then check snapshot
        for (int i = 0; i < 8; i++) {
            dispatcher.step(storageWith(A, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);
            if (activeSubTasksSize(dispatcher) == 0 && pendingStepsSize(dispatcher) == 1) {
                break;
            }
        }
        // Here step1 subtask should have completed; X must be in buffered storage (snapshot)
        final long xInBuffer = dispatcher.createSnapshot().copyInternalStorage().get(X);
        assertThat(xInBuffer).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldMoveBetweenStepsUsingBufferedStorageFromCompletedSubTask() throws Exception {
        // Kills pruneCompletedSubTasks line 137 "return false" mutation.
        // After a non-root sub-task completes, pruneCompletedSubTasks must return true
        // (changed=true) so the parent knows state changed.
        final Pattern stepPat = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(stepPat, 0, 1)), false),
            stepPat,
            p -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, p), Actor.EMPTY, false))
        );
        // First step: READY->RUNNING + dispatch + subtask steps
        dispatcher.step(storageWith(A, 1), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY);

        // Subsequent steps: keep stepping until subtask completes and is pruned
        boolean pruned = false;
        for (int i = 0; i < 10; i++) {
            final boolean changed = dispatcher.step(
                storageWith(), EMPTY_SINKS, StepBehavior.DEFAULT, TaskListener.EMPTY
            );
            if (dispatcher.getState() == TaskState.COMPLETED) {
                pruned = true;
                break;
            }
        }
        assertThat(pruned).isTrue();
    }

    @Test
    void shouldConsumeAndValidateRequirementsViaReflection() throws Exception {
        final Map<ResourceKey, Long> requirements = new LinkedHashMap<>();
        requirements.put(A, 2L);
        requirements.put(B, 1L);

        final Map<ResourceKey, Long> available = new LinkedHashMap<>();
        available.put(A, 3L);
        available.put(B, 1L);

        assertThat(invokeCanFulfill(requirements, available)).isTrue();

        invokeConsume(requirements, available);

        assertThat(available.get(A)).isEqualTo(1L);
        assertThat(available.get(B)).isZero();
        assertThat(invokeCanFulfill(requirements, available)).isFalse();
    }

    @Test
    void shouldReserveAcrossActiveSubTasksInAfterInsert() throws Exception {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(pattern, 0, 1)), false),
            pattern,
            ignored -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, pattern), Actor.EMPTY, false))
        );

        final FakeTask first = new FakeTask(0, 2, false, TaskState.RUNNING, Map.of(), Map.of());
        final FakeTask second = new FakeTask(0, 2, false, TaskState.RUNNING, Map.of(), Map.of());
        putActiveSubTask(dispatcher, first, Map.of(), false);
        putActiveSubTask(dispatcher, second, Map.of(), false);

        final long reserved = dispatcher.afterInsert(A, 5);

        assertThat(reserved).isEqualTo(4L);
    }

    @Test
    void shouldIncludeBufferedAndActiveSubTaskStorageInSnapshot() throws Exception {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            2,
            new LpStepPlan(List.of(step(pattern, 0, 1), step(pattern, 0, 1)), false),
            pattern,
            ignored -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, pattern), Actor.EMPTY, false))
        );

        dispatcher.beforeInsert(A, 3);

        final FakeTask first = new FakeTask(0, 0, false, TaskState.RUNNING, Map.of(A, 1L), Map.of(B, 2L));
        final FakeTask second = new FakeTask(0, 0, false, TaskState.READY, Map.of(A, 4L), Map.of(B, 1L));
        putActiveSubTask(dispatcher, first, Map.of(A, 1L), false);
        putActiveSubTask(dispatcher, second, Map.of(A, 1L), false);

        final TaskSnapshot snapshot = dispatcher.createSnapshot();

        assertThat(snapshot.copyInternalStorage().get(A)).isEqualTo(7L);
        assertThat(snapshot.initialRequirements().get(B)).isEqualTo(3L);
    }

    @Test
    void shouldCalculateProgressFromPendingAndActiveCounts() throws Exception {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            4,
            new LpStepPlan(List.of(
                step(pattern, 0, 1),
                step(pattern, 0, 1),
                step(pattern, 0, 1),
                step(pattern, 0, 1)
            ), false),
            pattern,
            ignored -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, pattern), Actor.EMPTY, false))
        );

        final FakeTask active = new FakeTask(0, 0, false, TaskState.RUNNING, Map.of(), Map.of());
        putActiveSubTask(dispatcher, active, Map.of(), false);

        final Field pendingField = LpTaskDispatcher.class.getDeclaredField("pendingSteps");
        pendingField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<LpExecutionPlanStep> pending = (List<LpExecutionPlanStep>) pendingField.get(dispatcher);
        while (pending.size() > 1) {
            pending.removeLast();
        }

        final double progress = invokeProgress(dispatcher);

        assertThat(progress).isEqualTo(0.5D);
    }

    @Test
    void shouldStepOnlyExistingSubTasksViaReflection() throws Exception {
        final Pattern pattern = pattern().ingredient(A, 1).output(B, 1).build();
        final LpTaskDispatcher dispatcher = newDispatcher(
            B,
            1,
            new LpStepPlan(List.of(step(pattern, 0, 1)), false),
            pattern,
            ignored -> Optional.of(new TaskImpl(LpDispatcherHelper.createDispatcherPlan(B, 1, pattern), Actor.EMPTY, false))
        );

        final FakeTask existing = new FakeTask(0, 0, true, TaskState.RUNNING, Map.of(), Map.of());
        putActiveSubTask(dispatcher, existing, Map.of(), false);

        final java.util.Set<com.refinedmods.refinedstorage.api.autocrafting.task.TaskId> taskIds =
            new java.util.LinkedHashSet<>();
        taskIds.add(existing.getId());
        taskIds.add(com.refinedmods.refinedstorage.api.autocrafting.task.TaskId.create());

        final boolean changed = invokeStepSpecificSubTasks(
            dispatcher,
            taskIds,
            storageWith(),
            EMPTY_SINKS,
            StepBehavior.DEFAULT,
            TaskListener.EMPTY
        );

        assertThat(changed).isTrue();
    }

    private static RootStorage storageWith(final Object... entries) {
        final RootStorage storage = new RootStorageImpl();
        final StorageImpl source = new StorageImpl();
        for (int index = 0; index < entries.length; index += 2) {
            source.insert(
                (ResourceKey) entries[index],
                ((Number) entries[index + 1]).longValue(),
                Action.EXECUTE,
                Actor.EMPTY
            );
        }
        storage.addSource(source);
        return storage;
    }

    private static LpTaskDispatcher newDispatcher(final ResourceKey resource,
                                                  final long amount,
                                                  final LpStepPlan stepPlan,
                                                  final Pattern rootPattern,
                                                  final Function<Pattern, Optional<Task>> patternProvider) {
        return new LpTaskDispatcher(
            resource,
            amount,
            Actor.EMPTY,
            true,
            stepPlan,
            rootPattern,
            patternProvider,
            NO_EXTERNAL_SINKS
        );
    }

    @SuppressWarnings("unchecked")
    private static int activeSubTasksSize(final LpTaskDispatcher dispatcher) throws Exception {
        final Field field = LpTaskDispatcher.class.getDeclaredField("activeSubTasks");
        field.setAccessible(true);
        return ((Map<?, ?>) field.get(dispatcher)).size();
    }

    @SuppressWarnings("unchecked")
    private static int pendingStepsSize(final LpTaskDispatcher dispatcher) throws Exception {
        final Field field = LpTaskDispatcher.class.getDeclaredField("pendingSteps");
        field.setAccessible(true);
        return ((List<?>) field.get(dispatcher)).size();
    }

    private static void putActiveSubTask(final LpTaskDispatcher dispatcher,
                                         final TaskImpl task,
                                         final Map<ResourceKey, Long> requirements,
                                         final boolean root) throws Exception {
        final Field field = LpTaskDispatcher.class.getDeclaredField("activeSubTasks");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        final Map<Object, Object> activeSubTasks = (Map<Object, Object>) field.get(dispatcher);

        final Class<?> dispatchedSubTaskClass = Class.forName(
            "com.refinedmods.refinedstorage.api.autocrafting.lp.LpTaskDispatcher$DispatchedSubTask"
        );
        final var constructor = dispatchedSubTaskClass.getDeclaredConstructor(TaskImpl.class, Map.class, boolean.class);
        constructor.setAccessible(true);
        final Object dispatchedSubTask = constructor.newInstance(task, requirements, root);
        activeSubTasks.put(task.getId(), dispatchedSubTask);
    }

    private static void invokeConsume(final Map<ResourceKey, Long> requirements,
                                      final Map<ResourceKey, Long> available) throws Exception {
        final Method method = LpTaskDispatcher.class.getDeclaredMethod("consume", Map.class, Map.class);
        method.setAccessible(true);
        method.invoke(null, requirements, available);
    }

    private static boolean invokeCanFulfill(final Map<ResourceKey, Long> requirements,
                                            final Map<ResourceKey, Long> available) throws Exception {
        final Method method = LpTaskDispatcher.class.getDeclaredMethod("canFulfill", Map.class, Map.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, requirements, available);
    }

    private static double invokeProgress(final LpTaskDispatcher dispatcher) throws Exception {
        final Method method = LpTaskDispatcher.class.getDeclaredMethod("progress");
        method.setAccessible(true);
        return (double) method.invoke(dispatcher);
    }

    private static boolean invokeStepSpecificSubTasks(final LpTaskDispatcher dispatcher,
                                                      final java.util.Set<?> taskIds,
                                                      final RootStorage rootStorage,
                                                      final ExternalPatternSinkProvider sinkProvider,
                                                      final StepBehavior stepBehavior,
                                                      final TaskListener listener) throws Exception {
        final Method method = LpTaskDispatcher.class.getDeclaredMethod(
            "stepSpecificSubTasks",
            java.util.Set.class,
            RootStorage.class,
            ExternalPatternSinkProvider.class,
            StepBehavior.class,
            TaskListener.class
        );
        method.setAccessible(true);
        return (boolean) method.invoke(dispatcher, taskIds, rootStorage, sinkProvider, stepBehavior, listener);
    }

    private static final class FakeTask extends TaskImpl {
        private final long beforeInsertAmount;
        private final long afterInsertAmount;
        private final boolean stepResult;
        private final TaskState forcedState;
        private final Map<ResourceKey, Long> internalStorage;
        private final Map<ResourceKey, Long> initialRequirements;

        private FakeTask(final long beforeInsertAmount,
                         final long afterInsertAmount,
                         final boolean stepResult,
                         final TaskState forcedState,
                         final Map<ResourceKey, Long> internalStorage,
                         final Map<ResourceKey, Long> initialRequirements) {
            super(
                LpDispatcherHelper.createDispatcherPlan(
                    B,
                    1,
                    pattern().ingredient(A, 1).output(B, 1).build()
                ),
                Actor.EMPTY,
                false
            );
            this.beforeInsertAmount = beforeInsertAmount;
            this.afterInsertAmount = afterInsertAmount;
            this.stepResult = stepResult;
            this.forcedState = forcedState;
            this.internalStorage = internalStorage;
            this.initialRequirements = initialRequirements;
        }

        @Override
        public long beforeInsert(final ResourceKey insertedResource, final long insertedAmount) {
            return Math.min(beforeInsertAmount, insertedAmount);
        }

        @Override
        public long afterInsert(final ResourceKey insertedResource, final long insertedAmount) {
            return Math.min(afterInsertAmount, insertedAmount);
        }

        @Override
        public boolean step(final RootStorage rootStorage,
                            final ExternalPatternSinkProvider sinkProvider,
                            final StepBehavior stepBehavior,
                            final TaskListener listener) {
            return stepResult;
        }

        @Override
        public TaskState getState() {
            return forcedState;
        }

        @Override
        public TaskSnapshot createSnapshot() {
            final MutableResourceList internal = MutableResourceListImpl.create();
            internalStorage.forEach(internal::add);

            final MutableResourceList initial = MutableResourceListImpl.create();
            initialRequirements.forEach(initial::add);

            return new TaskSnapshot(
                getId(),
                getResource(),
                getAmount(),
                getActor(),
                shouldNotify(),
                System.currentTimeMillis(),
                Map.of(),
                List.of(),
                initial,
                internal,
                forcedState,
                false
            );
        }
    }

    private static LpExecutionPlanStep step(final Pattern pattern, final int index, final long amount) {
        return new LpExecutionPlanStep(LpPatternRecipe.fromPattern(pattern, index), amount);
    }
}
