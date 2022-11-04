package com.refinedmods.refinedstorage2.platform.common.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class InterfaceExternalStorageProviderProxy<T> implements ExternalStorageProvider<T> {
    private final Level level;
    private final BlockPos pos;

    public InterfaceExternalStorageProviderProxy(final Level level, final BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @SuppressWarnings("unchecked")
    private Optional<InterfaceExternalStorageProvider<T>> tryGet() {
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity blockEntity) {
            return Optional.of(
                (InterfaceExternalStorageProvider<T>) blockEntity.getExternalStorageProvider()
            );
        }
        return Optional.empty();
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return tryGet().map(provider -> provider.extract(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return tryGet().map(provider -> provider.insert(resource, amount, action, actor)).orElse(0L);
    }

    @Override
    public Iterator<ResourceAmount<T>> iterator() {
        return tryGet().map(InterfaceExternalStorageProvider::iterator).orElse(Collections.emptyIterator());
    }
}
