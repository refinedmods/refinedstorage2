package com.refinedmods.refinedstorage2.platform.common.support.energy;

import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createStoredWithCapacityTranslation;

public class EnergyInfo {
    private final Player player;
    private final LongSupplier storedSupplier;
    private final LongSupplier capacitySupplier;
    private final RateLimiter rateLimiter = RateLimiter.create(4);

    private long stored;
    private long capacity;

    private EnergyInfo(final ServerPlayer player,
                       final LongSupplier storedSupplier,
                       final LongSupplier capacitySupplier) {
        this.player = player;
        this.storedSupplier = storedSupplier;
        this.capacitySupplier = capacitySupplier;
        this.stored = storedSupplier.getAsLong();
        this.capacity = capacitySupplier.getAsLong();
    }

    private EnergyInfo(final Player player, final long stored, final long capacity) {
        this.player = player;
        this.storedSupplier = () -> 0L;
        this.capacitySupplier = () -> 0L;
        this.stored = stored;
        this.capacity = capacity;
    }

    public void detectChanges() {
        final long newStored = storedSupplier.getAsLong();
        final long newCapacity = capacitySupplier.getAsLong();
        final boolean changed = stored != newStored || capacity != newCapacity;
        if (changed && rateLimiter.tryAcquire()) {
            setEnergy(newStored, newCapacity);
            Platform.INSTANCE.getServerToClientCommunications().sendEnergyInfo(
                (ServerPlayer) player,
                newStored,
                newCapacity
            );
        }
    }

    public void setEnergy(final long newStored, final long newCapacity) {
        this.stored = newStored;
        this.capacity = newCapacity;
    }

    public List<Component> createTooltip() {
        return Collections.singletonList(createStoredWithCapacityTranslation(
            stored,
            capacity,
            getPercentageFull()
        ));
    }

    public double getPercentageFull() {
        return (double) stored / (double) capacity;
    }

    public static EnergyInfo forServer(final Player player,
                                       final LongSupplier storedSupplier,
                                       final LongSupplier capacitySupplier) {
        return new EnergyInfo((ServerPlayer) player, storedSupplier, capacitySupplier);
    }

    public static EnergyInfo forClient(final Player player, final long stored, final long capacity) {
        return new EnergyInfo(player, stored, capacity);
    }
}
