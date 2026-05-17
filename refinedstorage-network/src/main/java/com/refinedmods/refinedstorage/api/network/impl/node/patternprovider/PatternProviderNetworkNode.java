package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkDetails;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkId;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.TaskContainer;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class PatternProviderNetworkNode extends SimpleNetworkNode implements PatternProvider, TaskListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternProviderNetworkNode.class);

    @Nullable
    private final Pattern[] patterns;
    private final Set<ParentContainer> parents = new HashSet<>();
    private final TaskContainer tasks = new TaskContainer(this);
    private int priority;
    @Nullable
    private PatternProviderExternalPatternSink sink;
    @Nullable
    private ExternalPatternSinkId id;
    @Nullable
    private Supplier<@Nullable ExternalPatternSinkDetails> detailsProvider;
    private StepBehavior stepBehavior = StepBehavior.DEFAULT;
    @Nullable
    private PatternProviderListener listener;

    public PatternProviderNetworkNode(final long energyUsage, final int patterns) {
        super(energyUsage);
        this.patterns = new Pattern[patterns];
    }

    public void setSink(final PatternProviderExternalPatternSink sink) {
        this.sink = sink;
    }

    public void tryUpdatePattern(final int index, @Nullable final Pattern pattern) {
        final Pattern oldPattern = patterns[index];
        if (oldPattern != null && oldPattern.equals(pattern)) {
            return;
        }
        if (oldPattern == null && pattern == null) {
            return;
        }
        LOGGER.debug("Detected pattern change at index {}: {} -> {}", index, oldPattern, pattern);
        if (oldPattern != null) {
            parents.forEach(parent -> parent.remove(this, oldPattern));
        }
        patterns[index] = pattern;
        if (pattern != null) {
            parents.forEach(parent -> parent.add(this, pattern, priority));
        }
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        if (this.network != null) {
            tasks.detachAll(this.network);
        }
        super.setNetwork(network);
        if (network != null) {
            tasks.attachAll(network);
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            for (final Pattern pattern : patterns) {
                if (pattern != null) {
                    parents.forEach(parent -> parent.remove(this, pattern));
                }
            }
            return;
        }
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parents.forEach(parent -> parent.add(this, pattern, priority));
            }
        }
    }

    @Override
    public void onAddedIntoContainer(final ParentContainer parentContainer) {
        parents.add(parentContainer);
        tasks.onAddedIntoContainer(parentContainer);
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parentContainer.add(this, pattern, priority);
            }
        }
    }

    @Override
    public void onRemovedFromContainer(final ParentContainer parentContainer) {
        tasks.onRemovedFromContainer(parentContainer);
        parents.remove(parentContainer);
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parentContainer.remove(this, pattern);
            }
        }
    }

    @Override
    public void addTask(final Task task) {
        tasks.add(task, network);
        parents.forEach(parent -> parent.taskAdded(this, task));
    }

    @Override
    public void cancelTask(final TaskId taskId) {
        tasks.cancel(taskId);
    }

    @Override
    public List<TaskStatus> getTaskStatuses() {
        return tasks.getStatuses();
    }

    @Override
    public long getAmount(final ResourceKey resource) {
        return tasks.getAmount(resource);
    }

    @Override
    public void receivedExternalIteration() {
        if (listener != null) {
            listener.receivedExternalIteration();
        }
    }

    @Override
    public void receivedExternalIteration(final Pattern pattern, final ExternalPatternSinkId sinkId) {
        if (network == null) {
            return;
        }
        final AutocraftingNetworkComponent autocrafting = network.getComponent(AutocraftingNetworkComponent.class);
        final PatternProvider provider = autocrafting.getProviderById(sinkId);
        if (provider == null) {
            return;
        }
        provider.receivedExternalIteration();
    }

    @Override
    public ExternalPatternSink.Result insertAll(final Pattern pattern,
                                                final Collection<ResourceAmount> resources,
                                                final Action action) {
        if (sink == null) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        return sink.insertAll(resources, action);
    }

    @Override
    public ExternalPatternSinkId getId() {
        return requireNonNull(id);
    }

    @Override
    @Nullable
    public ExternalPatternSinkDetails getDetails() {
        return detailsProvider != null ? detailsProvider.get() : null;
    }

    public void setId(final ExternalPatternSinkId id) {
        this.id = id;
    }

    public void setDetailsProvider(final Supplier<@Nullable ExternalPatternSinkDetails> detailsProvider) {
        this.detailsProvider = detailsProvider;
    }

    public List<Task> getTasks() {
        return tasks.getAll();
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive()) {
            return;
        }
        tasks.step(network, stepBehavior, this);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parents.forEach(parent -> parent.update(pattern, priority));
            }
        }
    }

    public void setStepBehavior(final StepBehavior stepBehavior) {
        this.stepBehavior = stepBehavior;
    }

    public void setListener(final PatternProviderListener listener) {
        this.listener = listener;
    }
}
