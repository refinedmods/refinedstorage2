package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FluidStorageBlockBlockEntity extends AbstractStorageBlockBlockEntity<FluidResource> {
    private final FluidStorageType.Variant variant;
    private final Component displayName;

    public FluidStorageBlockBlockEntity(final BlockPos pos,
                                        final BlockState state,
                                        final FluidStorageType.Variant variant) {
        super(
            BlockEntities.INSTANCE.getFluidStorageBlock(variant),
            pos,
            state,
            new StorageNetworkNode<>(getEnergyUsage(variant), StorageChannelTypes.FLUID),
            PlatformApi.INSTANCE.getFluidResourceFactory()
        );
        this.variant = variant;
        this.displayName = createTranslation(
            "block",
            String.format("%s_fluid_storage_block", variant.getName())
        );
    }

    private static long getEnergyUsage(final FluidStorageType.Variant variant) {
        return switch (variant) {
            case SIXTY_FOUR_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get64bEnergyUsage();
            case TWO_HUNDRED_FIFTY_SIX_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get256bEnergyUsage();
            case THOUSAND_TWENTY_FOUR_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get1024bEnergyUsage();
            case FOUR_THOUSAND_NINETY_SIX_B ->
                Platform.INSTANCE.getConfig().getFluidStorageBlock().get4096bEnergyUsage();
            case CREATIVE -> Platform.INSTANCE.getConfig().getFluidStorageBlock().getCreativeEnergyUsage();
        };
    }

    @Override
    protected Storage<FluidResource> createStorage(final Runnable listener) {
        return StorageTypes.FLUID.create(variant.getCapacity(), listener);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new FluidStorageBlockContainerMenu(
            syncId,
            player,
            getFilterContainer(),
            configContainer
        );
    }
}
