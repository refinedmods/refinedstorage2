package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import net.minecraft.world.entity.player.Player;

class AutocraftOnMissingResourcesConstructorStrategy implements ConstructorStrategy {
    private final ConstructorStrategy delegate;

    AutocraftOnMissingResourcesConstructorStrategy(final ConstructorStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public Result apply(final ResourceKey resource, final Actor actor, final Player player, final Network network) {
        final Result result = delegate.apply(resource, actor, player, network);
        if (result == Result.RESOURCE_MISSING) {
            final AutocraftingNetworkComponent autocrafting = network.getComponent(
                AutocraftingNetworkComponent.class
            );
            if (!autocrafting.getPatternsByOutput(resource).isEmpty()) {
                final long amount = resource instanceof PlatformResourceKey platformResource
                    ? platformResource.getResourceType().normalizeAmount(1.0D)
                    : 1L;
                final var ensureResult = autocrafting.ensureTask(resource, amount, actor,
                    new TimeoutableCancellationToken());
                final boolean success = ensureResult == AutocraftingNetworkComponent.EnsureResult.TASK_CREATED
                    || ensureResult == AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING;
                return success
                    ? ConstructorStrategy.Result.AUTOCRAFTING_STARTED
                    : ConstructorStrategy.Result.AUTOCRAFTING_MISSING_RESOURCES;
            }
        }
        return result;
    }
}
