package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridExtendedMenuProvider implements ExtendedMenuProvider {
    private final Grid grid;
    private final PlatformRegistry<PlatformStorageChannelType<?>> storageChannelTypeRegistry;
    private final MenuProvider menuProvider;

    public GridExtendedMenuProvider(final Grid grid,
                                    final PlatformRegistry<PlatformStorageChannelType<?>> storageChannelTypeRegistry,
                                    final MenuProvider menuProvider) {
        this.grid = grid;
        this.storageChannelTypeRegistry = storageChannelTypeRegistry;
        this.menuProvider = menuProvider;
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeBoolean(grid.isGridActive());
        final List<PlatformStorageChannelType<?>> types = storageChannelTypeRegistry.getAll();
        buf.writeInt(types.size());
        types.forEach(type -> writeStorageChannel(type, buf));
        if (menuProvider instanceof ExtendedMenuProvider extendedMenuProvider) {
            extendedMenuProvider.writeScreenOpeningData(player, buf);
        }
    }

    private <T> void writeStorageChannel(final PlatformStorageChannelType<T> storageChannelType,
                                         final FriendlyByteBuf buf) {
        final ResourceLocation id = storageChannelTypeRegistry.getId(storageChannelType).orElseThrow();
        buf.writeResourceLocation(id);
        final List<TrackedResourceAmount<T>> resources = grid.getResources(storageChannelType, PlayerActor.class);
        buf.writeInt(resources.size());
        resources.forEach(resource -> writeGridResource(storageChannelType, resource, buf));
    }

    private <T> void writeGridResource(final PlatformStorageChannelType<T> storageChannelType,
                                       final TrackedResourceAmount<T> resource,
                                       final FriendlyByteBuf buf) {
        storageChannelType.toBuffer(resource.resourceAmount().getResource(), buf);
        buf.writeLong(resource.resourceAmount().getAmount());
        PacketUtil.writeTrackedResource(buf, resource.trackedResource());
    }

    @Override
    public Component getDisplayName() {
        return menuProvider.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return menuProvider.createMenu(syncId, inventory, player);
    }
}
