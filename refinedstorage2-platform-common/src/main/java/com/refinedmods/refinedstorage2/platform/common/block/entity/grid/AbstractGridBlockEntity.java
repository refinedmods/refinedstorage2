package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractGridBlockEntity<T>
    extends AbstractInternalNetworkNodeContainerBlockEntity<GridNetworkNode<T>>
    implements ExtendedMenuProvider {
    protected AbstractGridBlockEntity(final BlockEntityType<?> type,
                                      final BlockPos pos,
                                      final BlockState state,
                                      final StorageChannelType<T> storageChannelType) {
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
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeBoolean(getNode().isActive());

        buf.writeInt(getNode().getResourceAmount());
        getNode().forEachResource((stack, trackedResource) -> {
            writeResourceAmount(buf, stack);
            PacketUtil.writeTrackedResource(buf, trackedResource.orElse(null));
        }, PlayerActor.class);
    }

    protected abstract void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<T> stack);

    public void addWatcher(final GridWatcher watcher) {
        getNode().addWatcher(watcher);
    }

    public void removeWatcher(final GridWatcher watcher) {
        getNode().removeWatcher(watcher);
    }
}
