package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

class InternalTaskPattern extends AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalTaskPattern.class);

    private final long originalIterationsRemaining;
    private long iterationsRemaining;

    InternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.originalIterationsRemaining = plan.iterations();
        this.iterationsRemaining = plan.iterations();
    }

    InternalTaskPattern(final TaskSnapshot.PatternSnapshot snapshot) {
        super(snapshot.pattern(), new TaskPlan.PatternPlan(
            snapshot.root(),
            requireNonNull(snapshot.internalPattern()).originalIterationsRemaining(),
            snapshot.ingredients()
        ));
        this.originalIterationsRemaining = snapshot.internalPattern().originalIterationsRemaining();
        this.iterationsRemaining = snapshot.internalPattern().iterationsRemaining();
    }

    @Override
    PatternStepResult step(final MutableResourceList internalStorage,
                           final RootStorage rootStorage,
                           final ExternalPatternSinkProvider sinkProvider,
                           final TaskListener listener) {
        final ResourceList iterationInputsSimulated = calculateIterationInputs(Action.SIMULATE);
        if (!extractAll(iterationInputsSimulated, internalStorage, Action.SIMULATE)) {
            return PatternStepResult.IDLE;
        }
        LOGGER.debug("Stepping {}", pattern);
        final ResourceList iterationInputs = calculateIterationInputs(Action.EXECUTE);
        extractAll(iterationInputs, internalStorage, Action.EXECUTE);
        pattern.layout().outputs().forEach(output -> returnOutput(internalStorage, rootStorage, output));
        pattern.layout().byproducts().forEach(byproduct -> returnOutput(internalStorage, rootStorage, byproduct));
        return useIteration();
    }

    private void returnOutput(final MutableResourceList internalStorage,
                              final RootStorage rootStorage,
                              final ResourceAmount output) {
        if (root) {
            LOGGER.debug("Inserting {}x {} into root storage", output.amount(), output.resource());
            final long inserted = rootStorage.insert(output.resource(), output.amount(), Action.EXECUTE, Actor.EMPTY);
            if (inserted != output.amount()) {
                final long remainder = output.amount() - inserted;
                LOGGER.debug("Inserting overflow {}x {} into internal storage", remainder, output.resource());
                internalStorage.add(output.resource(), remainder);
            }
        } else {
            LOGGER.debug("Inserting {}x {} into internal storage", output.amount(), output.resource());
            internalStorage.add(output);
        }
    }

    @Override
    void appendStatus(final TaskStatusBuilder builder) {
        if (iterationsRemaining == 0) {
            return;
        }
        for (final ResourceAmount output : pattern.layout().outputs()) {
            builder.crafting(output.resource(), output.amount() * iterationsRemaining);
        }
    }

    @Override
    long getWeight() {
        return originalIterationsRemaining;
    }

    @Override
    double getPercentageCompleted() {
        final long iterationsCompleted = originalIterationsRemaining - iterationsRemaining;
        return iterationsCompleted / (double) originalIterationsRemaining;
    }

    protected PatternStepResult useIteration() {
        iterationsRemaining--;
        LOGGER.debug("Stepped {} with {} iterations remaining", pattern, iterationsRemaining);
        return iterationsRemaining == 0 ? PatternStepResult.COMPLETED : PatternStepResult.RUNNING;
    }

    @Override
    TaskSnapshot.PatternSnapshot createSnapshot() {
        return new TaskSnapshot.PatternSnapshot(
            root,
            pattern,
            ingredients,
            new TaskSnapshot.InternalPatternSnapshot(originalIterationsRemaining, iterationsRemaining),
            null
        );
    }
}
