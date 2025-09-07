package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface AutocraftingNetworkComponent
    extends NetworkComponent, PreviewProvider, TaskStatusProvider, ExternalPatternSinkProvider {
    void addListener(PatternListener listener);

    void removeListener(PatternListener listener);

    Set<Pattern> getPatterns();

    List<Pattern> getPatternsByOutput(ResourceKey output);

    Set<ResourceKey> getOutputs();

    boolean contains(AutocraftingNetworkComponent component);

    @Nullable
    PatternProvider getProviderByPattern(Pattern pattern);

    EnsureResult ensureTask(ResourceKey resource, long amount, Actor actor, CancellationToken cancellationToken);

    @Deprecated // use the other overload with CancellationToken
    default EnsureResult ensureTask(ResourceKey resource, long amount, Actor actor) {
        return ensureTask(resource, amount, actor, CancellationToken.NONE);
    }

    enum EnsureResult {
        MISSING_RESOURCES,
        TASK_ALREADY_RUNNING,
        TASK_CREATED
    }
}
