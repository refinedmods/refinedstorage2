package com.refinedmods.refinedstorage2.platform.common.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.AbstractNetworkNodeContainerBlockEntity;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

class InterfaceProxyExternalStorageProvider<T> implements InterfaceExternalStorageProvider<T> {
    private final Level level;
    private final BlockPos pos;
    private final StorageChannelType<T> storageChannelType;

    InterfaceProxyExternalStorageProvider(final Level level,
                                          final BlockPos pos,
                                          final StorageChannelType<T> storageChannelType) {
        this.level = level;
        this.pos = pos;
        this.storageChannelType = storageChannelType;
    }

    private Optional<InterfaceBlockEntity> tryGetInterface() {
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity blockEntity) {
            return Optional.of(blockEntity);
        }
        return Optional.empty();
    }

    private Optional<InterfaceExternalStorageProvider<T>> tryGetProvider() {
        return tryGetInterface().map(iface -> iface.getExternalStorageProvider(storageChannelType));
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.extract(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.insert(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        return tryGetProvider().map(InterfaceExternalStorageProvider::iterator).orElse(Collections.emptyIterator());
    }

    @Override
    @Nullable
    public InterfaceNetworkNode getInterface() {
        return tryGetInterface().map(AbstractNetworkNodeContainerBlockEntity::getNode).orElse(null);
    }
}
