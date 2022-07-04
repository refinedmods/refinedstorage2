package com.refinedmods.refinedstorage2.platform.common.block.entity.storage;

import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

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
            FluidResourceType.INSTANCE
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
    protected PlatformStorage<FluidResource> createStorage(final Runnable listener) {
        final TrackedStorageRepository<FluidResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (!variant.hasCapacity()) {
            final TrackedStorageImpl<FluidResource> delegate = new TrackedStorageImpl<>(
                new InMemoryStorageImpl<>(),
                trackingRepository,
                System::currentTimeMillis
            );
            return new PlatformStorage<>(delegate, FluidStorageType.INSTANCE, trackingRepository, listener);
        }
        final LimitedStorageImpl<FluidResource> delegate = new LimitedStorageImpl<>(
            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
            variant.getCapacityInBuckets() * Platform.INSTANCE.getBucketAmount()
        );
        return new LimitedPlatformStorage<>(delegate, FluidStorageType.INSTANCE, trackingRepository, listener);
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
            resourceFilterContainer,
            this
        );
    }
}
