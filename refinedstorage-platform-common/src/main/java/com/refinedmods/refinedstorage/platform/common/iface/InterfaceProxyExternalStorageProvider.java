package com.refinedmods.refinedstorage.platform.common.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

class InterfaceProxyExternalStorageProvider implements InterfaceExternalStorageProvider {
    private final Level level;
    private final BlockPos pos;

    InterfaceProxyExternalStorageProvider(final Level level, final BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    private Optional<InterfaceBlockEntity> tryGetInterface() {
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity blockEntity) {
            return Optional.of(blockEntity);
        }
        return Optional.empty();
    }

    private Optional<InterfaceExternalStorageProvider> tryGetProvider() {
        return tryGetInterface().map(InterfaceBlockEntity::getExternalStorageProvider);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.extract(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.insert(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public Iterator<ResourceAmount> iterator() {
        return tryGetProvider().map(InterfaceExternalStorageProvider::iterator).orElse(Collections.emptyIterator());
    }

    @Override
    @Nullable
    public InterfaceNetworkNode getInterface() {
        return tryGetInterface().map(InterfaceBlockEntity::getInterface).orElse(null);
    }
}
