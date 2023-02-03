package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class GridBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<GridNetworkNode>
    implements ExtendedMenuProvider {
    private final PlatformRegistry<PlatformStorageChannelType<?>> storageChannelTypeRegistry;

    public GridBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getGrid(), pos, state, new GridNetworkNode(
            Platform.INSTANCE.getConfig().getGrid().getEnergyUsage(),
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll()
        ));
        this.storageChannelTypeRegistry = PlatformApi.INSTANCE.getStorageChannelTypeRegistry();
    }

    public ExtractableStorage<ItemResource> getContainerExtractionSource() {
        return Objects.requireNonNull(getNode().getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "grid");
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeBoolean(getNode().isActive());
        final List<PlatformStorageChannelType<?>> types = storageChannelTypeRegistry.getAll();
        buf.writeInt(types.size());
        types.forEach(type -> writeStorageChannel(type, buf));
    }

    private <T> void writeStorageChannel(final PlatformStorageChannelType<T> storageChannelType,
                                         final FriendlyByteBuf buf) {
        final ResourceLocation id = storageChannelTypeRegistry.getId(storageChannelType).orElseThrow();
        buf.writeResourceLocation(id);
        final List<GridNetworkNode.GridResource<T>> resources = getNode().getResources(
            storageChannelType,
            PlayerActor.class
        );
        buf.writeInt(resources.size());
        resources.forEach(resource -> writeGridResource(storageChannelType, resource, buf));
    }

    private <T> void writeGridResource(final PlatformStorageChannelType<T> storageChannelType,
                                       final GridNetworkNode.GridResource<T> resource,
                                       final FriendlyByteBuf buf) {
        storageChannelType.toBuffer(resource.resourceAmount().getResource(), buf);
        buf.writeLong(resource.resourceAmount().getAmount());
        PacketUtil.writeTrackedResource(buf, resource.trackedResource());
    }

    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        getNode().addWatcher(watcher, actorType);
    }

    public void removeWatcher(final GridWatcher watcher) {
        getNode().removeWatcher(watcher);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new GridContainerMenu(syncId, inventory, this);
    }
}
