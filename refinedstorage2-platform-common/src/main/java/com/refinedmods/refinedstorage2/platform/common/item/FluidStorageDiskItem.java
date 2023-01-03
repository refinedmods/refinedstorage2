package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FluidStorageDiskItem extends AbstractStorageContainerItem<FluidResource> {
    private final FluidStorageType.Variant variant;

    public FluidStorageDiskItem(final CreativeModeTab tab, final FluidStorageType.Variant variant) {
        super(
            new Item.Properties().tab(tab).stacksTo(1).fireResistant(),
            StorageChannelTypes.FLUID,
            PlatformApi.INSTANCE.getStorageContainerHelper()
        );
        this.variant = variant;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        if (level == null) {
            return;
        }
        final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        final boolean showCapacityAndProgress = variant != FluidStorageType.Variant.CREATIVE;
        helper.appendToTooltip(
            stack,
            storageRepository,
            tooltip,
            context,
            Platform.INSTANCE.getBucketQuantityFormatter()::formatWithUnits,
            Platform.INSTANCE.getBucketQuantityFormatter()::format,
            showCapacityAndProgress,
            false
        );
    }

    @Override
    public boolean hasStacking() {
        return false;
    }

    @Override
    protected Storage<FluidResource> createStorage(final StorageRepository storageRepository) {
        final TrackedStorageRepository<FluidResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (!variant.hasCapacity()) {
            final TrackedStorageImpl<FluidResource> delegate = new TrackedStorageImpl<>(
                new InMemoryStorageImpl<>(),
                trackingRepository,
                System::currentTimeMillis
            );
            return new PlatformStorage<>(
                delegate,
                FluidStorageType.INSTANCE,
                trackingRepository,
                storageRepository::markAsChanged
            );
        }
        final LimitedStorageImpl<FluidResource> delegate = new LimitedStorageImpl<>(
            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
            variant.getCapacityInBuckets() * Platform.INSTANCE.getBucketAmount()
        );
        return new LimitedPlatformStorage<>(
            delegate,
            FluidStorageType.INSTANCE,
            trackingRepository,
            storageRepository::markAsChanged
        );
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Items.INSTANCE.getStorageHousing(), count);
    }

    @Override
    @Nullable
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        if (variant == FluidStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getFluidStoragePart(variant), count);
    }
}
