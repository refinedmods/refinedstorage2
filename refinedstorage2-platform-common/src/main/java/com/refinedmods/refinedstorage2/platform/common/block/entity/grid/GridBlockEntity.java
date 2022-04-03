package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class GridBlockEntity<T> extends InternalNetworkNodeContainerBlockEntity<GridNetworkNode<T>> implements ExtendedMenuProvider {
    protected GridBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, StorageChannelType<T> storageChannelType) {
        super(type, pos, state, new GridNetworkNode<>(
                Platform.INSTANCE.getConfig().getGrid().getEnergyUsage(),
                storageChannelType
        ));
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "grid");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBoolean(getNode().isActive());

        buf.writeInt(getNode().getResourceCount());
        getNode().forEachResource((stack, trackedResource) -> {
            writeResourceAmount(buf, stack);
            PacketUtil.writeTrackedResource(buf, trackedResource.orElse(null));
        }, PlayerSource.class);
    }

    protected abstract void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<T> stack);

    public void addWatcher(GridWatcher watcher) {
        getNode().addWatcher(watcher);
    }

    public void removeWatcher(GridWatcher watcher) {
        getNode().removeWatcher(watcher);
    }
}
