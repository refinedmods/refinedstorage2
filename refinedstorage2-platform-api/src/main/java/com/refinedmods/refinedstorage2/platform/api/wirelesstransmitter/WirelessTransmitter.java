package com.refinedmods.refinedstorage2.platform.api.wirelesstransmitter;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
@FunctionalInterface
public interface WirelessTransmitter {
    boolean isInRange(ResourceKey<Level> dimension, Vec3 position);
}
