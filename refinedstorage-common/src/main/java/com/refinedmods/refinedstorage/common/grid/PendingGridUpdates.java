package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.HashMap;
import java.util.Map;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

class PendingGridUpdates {
    private static final int MAX_UPDATES_PER_SECOND_PER_RESOURCE_TYPE = 4;

    private final ServerPlayer serverPlayer;
    private final Map<Class<? extends ResourceKey>, RateLimiter> otherRateLimiters = new HashMap<>();
    private final RateLimiter itemRateLimiter = createRateLimiter();
    private final RateLimiter fluidRateLimiter = createRateLimiter();
    private final Map<PlatformResourceKey, PendingUpdate> pendingUpdates = new HashMap<>();
    private final RateLimiter flushQueueRateLimiter = RateLimiter.create(1);

    PendingGridUpdates(final ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    void tryFlush() {
        if (pendingUpdates.isEmpty() || !flushQueueRateLimiter.tryAcquire()) {
            return;
        }
        for (final Map.Entry<PlatformResourceKey, PendingUpdate> entry : pendingUpdates.entrySet()) {
            final PlatformResourceKey resource = entry.getKey();
            final PendingUpdate update = entry.getValue();
            if (update.change == 0) {
                continue;
            }
            sendUpdate(update.change, update.trackedResource, resource);
        }
        pendingUpdates.clear();
    }

    void onChanged(final ResourceKey resource, final long change, @Nullable final TrackedResource trackedResource) {
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return;
        }
        final RateLimiter rateLimiter = getRateLimiter(resource.getClass());
        if (!rateLimiter.tryAcquire()) {
            queueUpdate(change, trackedResource, platformResource);
            return;
        }
        sendUpdate(change, trackedResource, platformResource);
    }

    private void queueUpdate(final long change, final @Nullable TrackedResource trackedResource,
                             final PlatformResourceKey platformResource) {
        final PendingUpdate existing = pendingUpdates.get(platformResource);
        if (existing != null) {
            existing.update(change, trackedResource);
        } else {
            pendingUpdates.put(platformResource, PendingUpdate.create(change, trackedResource));
        }
    }

    private void sendUpdate(final long change, final @Nullable TrackedResource trackedResource,
                            final PlatformResourceKey platformResource) {
        S2CPackets.sendGridUpdate(serverPlayer, platformResource, change, trackedResource);
    }

    private RateLimiter getRateLimiter(final Class<? extends ResourceKey> resourceType) {
        if (resourceType == ItemResource.class) {
            return itemRateLimiter;
        } else if (resourceType == FluidResource.class) {
            return fluidRateLimiter;
        } else {
            return otherRateLimiters.computeIfAbsent(resourceType, k -> createRateLimiter());
        }
    }

    private static RateLimiter createRateLimiter() {
        return RateLimiter.create(MAX_UPDATES_PER_SECOND_PER_RESOURCE_TYPE);
    }

    private static class PendingUpdate {
        private long change;
        @Nullable
        private TrackedResource trackedResource;

        void update(final long newChange, @Nullable final TrackedResource newTrackedResource) {
            this.change += newChange;
            this.trackedResource = newTrackedResource;
        }

        private static PendingUpdate create(final long change, @Nullable final TrackedResource trackedResource) {
            final PendingUpdate pendingUpdate = new PendingUpdate();
            pendingUpdate.change = change;
            pendingUpdate.trackedResource = trackedResource;
            return pendingUpdate;
        }
    }
}
