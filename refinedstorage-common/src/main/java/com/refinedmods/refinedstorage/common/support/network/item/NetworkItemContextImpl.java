package com.refinedmods.refinedstorage.common.support.network.item;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemPlayerValidator;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

class NetworkItemContextImpl implements NetworkItemContext {
    private final Player player;
    private final NetworkItemPlayerValidator.PlayerCoordinates coordinates;
    private final SlotReference slotReference;
    @Nullable
    private final GlobalPos networkLocation;

    @SuppressWarnings("resource")
    NetworkItemContextImpl(
        final Player player,
        final SlotReference slotReference,
        @Nullable final GlobalPos networkLocation
    ) {
        this.player = player;
        // We copy the player position and dimension
        // as it can change after opening the network bound item (opening while walking)
        // and could cause the network not being accessible anymore (due to being out of range of a transmitter).
        // If the network is no longer accessible, certain assumptions will break (e.g. grid watcher can no longer
        // be removed after it was added).
        this.coordinates = new NetworkItemPlayerValidator.PlayerCoordinates(
            player.level().dimension(),
            new Vec3(player.position().x, player.position().y, player.position().z)
        );
        this.slotReference = slotReference;
        this.networkLocation = networkLocation;
    }

    @Override
    public Optional<Network> resolveNetwork() {
        if (networkLocation == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(player.level().getServer())
            .map(server -> server.getLevel(networkLocation.dimension()))
            .filter(level -> level.isLoaded(networkLocation.pos()))
            .map(level -> level.getBlockEntity(networkLocation.pos()))
            .filter(NetworkItemTargetBlockEntity.class::isInstance)
            .map(NetworkItemTargetBlockEntity.class::cast)
            .map(NetworkItemTargetBlockEntity::getNetworkForItem)
            .filter(this::isValid);
    }

    private boolean isValid(final Network network) {
        return network.getComponent(GraphNetworkComponent.class)
            .getContainers(NetworkItemPlayerValidator.class)
            .stream()
            .anyMatch(validator -> validator.isValid(coordinates));
    }

    @Override
    public boolean isActive() {
        return slotReference.resolve(player).map(stack -> {
            if (!Platform.INSTANCE.getConfig().isRequireEnergy()) {
                return true;
            }
            return Platform.INSTANCE.getEnergyStorage(stack)
                .map(energyStorage -> energyStorage.getStored() > 0)
                .orElse(false);
        }).orElse(false);
    }

    @Override
    public void drainEnergy(final long amount) {
        slotReference.resolve(player).flatMap(Platform.INSTANCE::getEnergyStorage).ifPresent(
            energyStorage -> energyStorage.extract(amount, Action.EXECUTE)
        );
    }
}
