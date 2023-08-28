package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NetworkBoundItemContext {
    private final Player player;
    private final Vec3 playerPosition;
    private final PlayerSlotReference playerSlotReference;
    @Nullable
    private final NetworkReference networkReference;

    public NetworkBoundItemContext(
        final Player player,
        final PlayerSlotReference playerSlotReference,
        @Nullable final NetworkReference networkReference
    ) {
        this.player = player;
        // We copy the player position as it can change after opening the network bound item (opening while walking)
        // and could cause the network not being accessible anymore (due to being out of range of a transmitter).
        // If the network is no longer accessible, certain assumptions will break (e.g. grid watcher can no longer
        // be removed after it was added).
        this.playerPosition = new Vec3(player.position().x, player.position().y, player.position().z);
        this.playerSlotReference = playerSlotReference;
        this.networkReference = networkReference;
    }

    public Vec3 getPlayerPosition() {
        return playerPosition;
    }

    public ResourceKey<Level> getPlayerLevel() {
        return player.level().dimension();
    }

    public boolean isActive() {
        return Platform.INSTANCE.getEnergyStorage(playerSlotReference.resolve(player))
            .map(energyStorage -> energyStorage.getStored() > 0)
            .orElse(true);
    }

    public void drain(final long amount) {
        Platform.INSTANCE.getEnergyStorage(playerSlotReference.resolve(player)).ifPresent(
            energyStorage -> energyStorage.extract(amount, Action.EXECUTE)
        );
    }

    @Nullable
    public NetworkReference getNetworkReference() {
        return networkReference;
    }

    public record NetworkReference(ResourceKey<Level> dimensionKey, BlockPos pos) {
    }
}
