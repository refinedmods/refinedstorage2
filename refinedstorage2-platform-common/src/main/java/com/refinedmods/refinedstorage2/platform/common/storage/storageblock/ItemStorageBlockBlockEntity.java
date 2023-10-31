package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ItemStorageBlockBlockEntity extends AbstractStorageBlockBlockEntity<ItemResource> {
    private final ItemStorageType.Variant variant;
    private final Component displayName;

    public ItemStorageBlockBlockEntity(final BlockPos pos,
                                       final BlockState state,
                                       final ItemStorageType.Variant variant) {
        super(
            BlockEntities.INSTANCE.getItemStorageBlock(variant),
            pos,
            state,
            new StorageNetworkNode<>(getEnergyUsage(variant), StorageChannelTypes.ITEM),
            PlatformApi.INSTANCE.getItemResourceFactory()
        );
        this.variant = variant;
        this.displayName = createTranslation("block", String.format("%s_storage_block", variant.getName()));
    }

    private static long getEnergyUsage(final ItemStorageType.Variant variant) {
        return switch (variant) {
            case ONE_K -> Platform.INSTANCE.getConfig().getStorageBlock().get1kEnergyUsage();
            case FOUR_K -> Platform.INSTANCE.getConfig().getStorageBlock().get4kEnergyUsage();
            case SIXTEEN_K -> Platform.INSTANCE.getConfig().getStorageBlock().get16kEnergyUsage();
            case SIXTY_FOUR_K -> Platform.INSTANCE.getConfig().getStorageBlock().get64kEnergyUsage();
            case CREATIVE -> Platform.INSTANCE.getConfig().getStorageBlock().getCreativeEnergyUsage();
        };
    }

    @Override
    protected Storage<ItemResource> createStorage(final Runnable listener) {
        return StorageTypes.ITEM.create(variant.getCapacity(), listener);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ItemStorageBlockContainerMenu(
            syncId,
            player,
            getFilterContainer(),
            configContainer
        );
    }
}
