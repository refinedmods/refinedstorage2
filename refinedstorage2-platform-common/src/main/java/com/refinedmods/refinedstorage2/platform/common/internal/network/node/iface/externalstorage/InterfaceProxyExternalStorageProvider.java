package com.refinedmods.refinedstorage2.platform.common.internal.network.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.blockentity.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class InterfaceProxyExternalStorageProvider implements InterfaceExternalStorageProvider<ItemResource> {
    private final Level level;
    private final BlockPos pos;

    public InterfaceProxyExternalStorageProvider(final Level level, final BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    private Optional<InterfaceBlockEntity> tryGetInterface() {
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity blockEntity) {
            return Optional.of(blockEntity);
        }
        return Optional.empty();
    }

    private Optional<InterfaceExternalStorageProvider<ItemResource>> tryGetProvider() {
        return tryGetInterface().map(InterfaceBlockEntity::getExternalStorageProvider);
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.extract(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return tryGetProvider().map(provider -> provider.insert(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public Iterator<ResourceAmount<ItemResource>> iterator() {
        return tryGetProvider().map(InterfaceExternalStorageProvider::iterator).orElse(Collections.emptyIterator());
    }

    @Override
    @Nullable
    public InterfaceNetworkNode<ItemResource> getInterface() {
        return tryGetInterface().map(AbstractNetworkNodeContainerBlockEntity::getNode).orElse(null);
    }
}
