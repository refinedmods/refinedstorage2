package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FluidStorageDiskItem extends AbstractStorageDiskItem<FluidResource> {
    private final FluidStorageType.Variant variant;
    private final Set<StorageTooltipHelper.TooltipOption> tooltipOptions =
        EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);

    public FluidStorageDiskItem(final CreativeModeTab tab, final FluidStorageType.Variant variant) {
        super(new Item.Properties().tab(tab).stacksTo(1).fireResistant(), StorageChannelTypes.FLUID);
        this.variant = variant;
        if (variant != FluidStorageType.Variant.CREATIVE) {
            this.tooltipOptions.add(StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS);
        }
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        StorageItemHelper.appendToTooltip(
            stack,
            level,
            tooltip,
            context,
            Platform.INSTANCE.getBucketQuantityFormatter()::formatWithUnits,
            Platform.INSTANCE.getBucketQuantityFormatter()::format,
            tooltipOptions
        );
    }

    @Override
    public boolean hasStacking() {
        return false;
    }

    @Override
    protected Storage<FluidResource> createStorage(final Level level) {
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
                PlatformApi.INSTANCE.getStorageRepository(level)::markAsChanged
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
            PlatformApi.INSTANCE.getStorageRepository(level)::markAsChanged
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
