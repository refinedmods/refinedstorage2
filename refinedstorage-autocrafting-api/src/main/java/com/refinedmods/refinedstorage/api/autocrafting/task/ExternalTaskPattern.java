package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

class ExternalTaskPattern extends AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalTaskPattern.class);

    private final MutableResourceList expectedOutputs;
    private final ResourceList simulatedIterationInputs;
    private final long originalIterationsToSendToSink;
    private long iterationsToSendToSink;
    private long iterationsReceived;
    private boolean interceptedAnythingSinceLastStep;
    private boolean interceptedAnIterationAtLeastOnceSinceLastStep;
    private ExternalPatternSink.@Nullable Result lastSinkResult;
    @Nullable
    private ExternalPatternSinkKey lastSinkResultKey;
    private int currentSinkIndex;

    ExternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.originalIterationsToSendToSink = plan.iterations();
        this.expectedOutputs = MutableResourceListImpl.create();
        pattern.layout().outputs().forEach(
            output -> expectedOutputs.add(output.resource(), output.amount() * plan.iterations())
        );
        this.iterationsToSendToSink = plan.iterations();
        this.simulatedIterationInputs = calculateIterationInputs(Action.SIMULATE);
    }

    ExternalTaskPattern(final TaskSnapshot.PatternSnapshot snapshot) {
        super(snapshot.pattern(), new TaskPlan.PatternPlan(
            snapshot.root(),
            requireNonNull(snapshot.externalPattern()).originalIterationsToSendToSink(),
            snapshot.ingredients()
        ));
        this.expectedOutputs = snapshot.externalPattern().copyExpectedOutputs();
        this.simulatedIterationInputs = snapshot.externalPattern().simulatedIterationInputs();
        this.originalIterationsToSendToSink = snapshot.externalPattern().originalIterationsToSendToSink();
        this.iterationsToSendToSink = snapshot.externalPattern().iterationsToSendToSink();
        this.iterationsReceived = snapshot.externalPattern().iterationsReceived();
        this.interceptedAnythingSinceLastStep = snapshot.externalPattern().interceptedAnythingSinceLastStep();
        this.lastSinkResult = snapshot.externalPattern().lastSinkResult();
        this.lastSinkResultKey = snapshot.externalPattern().lastSinkResultKey();
    }

    @Override
    PatternStepResult step(final MutableResourceList internalStorage,
                           final RootStorage rootStorage,
                           final ExternalPatternSinkProvider sinkProvider,
                           final TaskListener listener) {
        if (interceptedAnIterationAtLeastOnceSinceLastStep) {
            interceptedAnIterationAtLeastOnceSinceLastStep = false;
            listener.receivedExternalIteration(pattern);
        }
        if (expectedOutputs.isEmpty()) {
            return PatternStepResult.COMPLETED;
        }
        if (iterationsToSendToSink == 0) {
            return idleOrRunning();
        }
        if (!acceptsIterationInputs(internalStorage, sinkProvider)) {
            return idleOrRunning();
        }
        LOGGER.debug("Stepped {} with {} iterations remaining", pattern, iterationsToSendToSink);
        iterationsToSendToSink--;
        interceptedAnythingSinceLastStep = false;
        return PatternStepResult.RUNNING;
    }

    private PatternStepResult idleOrRunning() {
        if (interceptedAnythingSinceLastStep) {
            interceptedAnythingSinceLastStep = false;
            return PatternStepResult.RUNNING;
        }
        return PatternStepResult.IDLE;
    }

    @Override
    long beforeInsert(final ResourceKey resource, final long amount) {
        if (root) {
            return 0;
        }
        return trySatisfy(resource, amount);
    }

    @Override
    long afterInsert(final ResourceKey resource, final long amount) {
        if (!root) {
            return 0;
        }
        return trySatisfy(resource, amount);
    }

    private long trySatisfy(final ResourceKey resource, final long amount) {
        final long needed = expectedOutputs.get(resource);
        if (needed == 0) {
            return 0;
        }
        final long correctedAmount = Math.min(needed, amount);
        expectedOutputs.remove(resource, correctedAmount);
        final boolean receivedAtLeastOneIteration = updateIterationsReceived();
        if (receivedAtLeastOneIteration) {
            interceptedAnIterationAtLeastOnceSinceLastStep = true;
        }
        interceptedAnythingSinceLastStep = true;
        return correctedAmount;
    }

    private boolean updateIterationsReceived() {
        final long originalIterationsReceived = iterationsReceived;
        long result = originalIterationsToSendToSink;
        for (final ResourceAmount output : pattern.layout().outputs()) {
            final long expected = output.amount() * originalIterationsToSendToSink;
            final long stillNeeded = expectedOutputs.get(output.resource());
            final long receivedOutputs = expected - stillNeeded;
            final long receivedOutputIterations = receivedOutputs / output.amount();
            if (result > receivedOutputIterations) {
                result = receivedOutputIterations;
            }
        }
        this.iterationsReceived = result;
        return iterationsReceived > originalIterationsReceived;
    }

    @Override
    void appendStatus(final TaskStatusBuilder builder) {
        final List<ResourceAmount> outputs = pattern.layout().outputs();
        if (iterationsToSendToSink > 0) {
            for (final ResourceAmount output : outputs) {
                builder.scheduled(output.resource(), output.amount() * iterationsToSendToSink);
            }
        }
        final long iterationsSentToSink = originalIterationsToSendToSink - iterationsToSendToSink;
        final long iterationsProcessing = iterationsSentToSink - iterationsReceived;
        if (iterationsProcessing > 0) {
            for (final ResourceKey input : simulatedIterationInputs.getAll()) {
                builder.processing(
                    input,
                    simulatedIterationInputs.get(input) * iterationsProcessing,
                    lastSinkResultKey
                );
            }
        }
        if (lastSinkResult != null) {
            switch (lastSinkResult) {
                case REJECTED -> outputs.stream().map(ResourceAmount::resource).forEach(builder::rejected);
                case SKIPPED -> outputs.stream().map(ResourceAmount::resource).forEach(builder::noneFound);
                case LOCKED -> outputs.stream().map(ResourceAmount::resource).forEach(builder::locked);
                case ACCEPTED -> {
                    // does not need to be reported
                }
            }
        }
    }

    @Override
    long getWeight() {
        return iterationsToSendToSink;
    }

    @Override
    double getPercentageCompleted() {
        return iterationsReceived / (double) originalIterationsToSendToSink;
    }

    private boolean acceptsIterationInputs(final MutableResourceList internalStorage,
                                           final ExternalPatternSinkProvider sinkProvider) {
        final ResourceList iterationInputsSimulated = calculateIterationInputs(Action.SIMULATE);
        if (!extractAll(iterationInputsSimulated, internalStorage, Action.SIMULATE)) {
            return false;
        }
        final List<ExternalPatternSink> sinks = sinkProvider.getSinksByPatternLayout(pattern.layout());
        if (sinks.isEmpty()) {
            lastSinkResult = ExternalPatternSink.Result.SKIPPED;
            return false;
        }
        final ExternalPatternSink sink = getSinkThatIsAcceptingResources(sinks, iterationInputsSimulated);
        if (sink == null) {
            return false;
        }
        final ResourceList iterationInputs = calculateIterationInputs(Action.EXECUTE);
        extractAll(iterationInputs, internalStorage, Action.EXECUTE);
        // If the sink does not accept the inputs
        // we cannot return the extracted resources to the internal storage
        // because we have already deducted from the iteration inputs
        // and because the sink might have still accepted some resources halfway.
        // If we returned the extracted resources to the internal storage and correct the
        // iteration inputs, it would potentially duplicate the resources
        // across the sink and the internal storage.
        // The end result is that we lie, do as if the insertion was successful,
        // and potentially void the extracted resources from the internal storage.
        if (sink.accept(pattern, iterationInputs.copyState(), Action.EXECUTE) != ExternalPatternSink.Result.ACCEPTED) {
            LOGGER.warn("Sink {} did not accept all inputs for pattern {}", sink, pattern);
        }
        return true;
    }

    @Nullable
    private ExternalPatternSink getSinkThatIsAcceptingResources(final List<ExternalPatternSink> sinks,
                                                                final ResourceList iterationInputsSimulated) {
        if (currentSinkIndex >= sinks.size()) {
            currentSinkIndex = 0;
        }
        while (currentSinkIndex < sinks.size()) {
            final ExternalPatternSink sink = sinks.get(currentSinkIndex);
            final ExternalPatternSink.Result simulatedResult = sink.accept(
                pattern,
                iterationInputsSimulated.copyState(),
                Action.SIMULATE
            );
            lastSinkResult = simulatedResult;
            lastSinkResultKey = sink.getKey();
            currentSinkIndex++;
            if (simulatedResult != ExternalPatternSink.Result.ACCEPTED) {
                continue;
            }
            return sink;
        }
        return null;
    }

    @Override
    TaskSnapshot.PatternSnapshot createSnapshot() {
        return new TaskSnapshot.PatternSnapshot(
            root,
            pattern,
            ingredients,
            null,
            new TaskSnapshot.ExternalPatternSnapshot(
                expectedOutputs.copy(),
                simulatedIterationInputs,
                originalIterationsToSendToSink,
                iterationsToSendToSink,
                iterationsReceived,
                interceptedAnythingSinceLastStep,
                lastSinkResult,
                lastSinkResultKey
            )
        );
    }
}
