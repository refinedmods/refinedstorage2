package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.TaskContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

class RelayOutputPatternProvider implements PatternProvider, PatternListener, TaskListener {
    private final RelayOutputNetworkNode outputNode;
    private final Filter filter = new Filter();
    private final Set<ParentContainer> parents = new HashSet<>();
    private final TaskContainer tasks = new TaskContainer(this);
    @Nullable
    private AutocraftingNetworkComponent delegate;
    private StepBehavior stepBehavior = StepBehavior.DEFAULT;

    RelayOutputPatternProvider(final RelayOutputNetworkNode outputNode) {
        this.outputNode = outputNode;
    }

    List<Task> getTasks() {
        return tasks.getAll();
    }

    void setFilters(final Set<ResourceKey> filters) {
        reset(() -> filter.setFilters(filters));
    }

    void setFilterMode(final FilterMode filterMode) {
        reset(() -> filter.setMode(filterMode));
    }

    void setFilterNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        reset(() -> filter.setNormalizer(normalizer));
    }

    private void reset(final Runnable action) {
        final AutocraftingNetworkComponent oldDelegate = delegate;
        setDelegate(null);
        action.run();
        setDelegate(oldDelegate);
    }

    void setDelegate(@Nullable final AutocraftingNetworkComponent delegate) {
        if (this.delegate != null) {
            parents.forEach(parent -> getPatterns().forEach(pattern -> parent.remove(this, pattern)));
            this.delegate.removeListener(this);
        }
        this.delegate = delegate;
        if (delegate != null) {
            parents.forEach(parent -> getPatterns().forEach(pattern -> parent.add(this, pattern, 0)));
            delegate.addListener(this);
        }
    }

    boolean hasDelegate() {
        return delegate != null;
    }

    private Set<Pattern> getPatterns() {
        if (delegate == null) {
            return Collections.emptySet();
        }
        return delegate.getPatterns().stream().filter(this::isPatternAllowed).collect(Collectors.toSet());
    }

    private boolean isPatternAllowed(final Pattern pattern) {
        return pattern.layout().outputs().stream().map(ResourceAmount::resource).anyMatch(filter::isAllowed);
    }

    @Override
    public void onAdded(final Pattern pattern) {
        if (delegate == null || !isPatternAllowed(pattern) || delegate.contains(delegate)) {
            return;
        }
        parents.forEach(parent -> parent.add(this, pattern, 0));
    }

    @Override
    public void onRemoved(final Pattern pattern) {
        if (delegate == null || !isPatternAllowed(pattern) || delegate.contains(delegate)) {
            return;
        }
        parents.forEach(parent -> parent.remove(this, pattern));
    }

    @Override
    public boolean contains(final AutocraftingNetworkComponent component) {
        return component == delegate || (delegate != null && delegate.contains(component));
    }

    @Override
    public void addTask(final Task task) {
        tasks.add(task, outputNode.getNetwork());
        parents.forEach(parent -> parent.taskAdded(this, task));
    }

    void doWork() {
        final Network network = outputNode.getNetwork();
        if (network == null) {
            return;
        }
        tasks.step(network, stepBehavior, this);
    }

    void setStepBehavior(final StepBehavior stepBehavior) {
        this.stepBehavior = stepBehavior;
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
        // no op
    }

    @Override
    public void receivedExternalIteration(final Pattern pattern) {
        if (delegate == null) {
            return;
        }
        final PatternProvider patternProvider = delegate.getProviderByPattern(pattern);
        if (patternProvider == null) {
            return;
        }
        patternProvider.receivedExternalIteration();
    }

    @Override
    public void onAddedIntoContainer(final ParentContainer parentContainer) {
        if (delegate != null) {
            delegate.getPatterns().forEach(pattern -> parentContainer.add(this, pattern, 0));
        }
        tasks.onAddedIntoContainer(parentContainer);
        parents.add(parentContainer);
    }

    @Override
    public void onRemovedFromContainer(final ParentContainer parentContainer) {
        if (delegate != null) {
            delegate.getPatterns().forEach(pattern -> parentContainer.remove(this, pattern));
        }
        tasks.onRemovedFromContainer(parentContainer);
        parents.remove(parentContainer);
    }

    @Override
    public ExternalPatternSink.Result accept(final Pattern pattern,
                                             final Collection<ResourceAmount> resources,
                                             final Action action) {
        if (delegate == null) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        final PatternProvider patternProvider = delegate.getProviderByPattern(pattern);
        if (patternProvider == null) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        return patternProvider.accept(pattern, resources, action);
    }

    void detachAll(final Network network) {
        tasks.detachAll(network);
    }

    void attachAll(final Network network) {
        tasks.attachAll(network);
    }
}
