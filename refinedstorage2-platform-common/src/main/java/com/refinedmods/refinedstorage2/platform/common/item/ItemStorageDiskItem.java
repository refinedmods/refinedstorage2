package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

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

public class ItemStorageDiskItem extends AbstractStorageContainerItem<ItemResource> {
    private final ItemStorageType.Variant variant;
    private final Set<StorageTooltipHelper.TooltipOption> tooltipOptions =
        EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);

    public ItemStorageDiskItem(final CreativeModeTab tab, final ItemStorageType.Variant variant) {
        super(new Item.Properties().tab(tab).stacksTo(1).fireResistant(), StorageChannelTypes.ITEM);
        this.variant = variant;
        this.tooltipOptions.add(StorageTooltipHelper.TooltipOption.STACK_INFO);
        if (variant != ItemStorageType.Variant.CREATIVE) {
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
            QuantityFormatter::formatWithUnits,
            QuantityFormatter::format,
            tooltipOptions
        );
    }

    @Override
    public boolean hasStacking() {
        return true;
    }

    @Override
    protected Storage<ItemResource> createStorage(final Level level) {
        final TrackedStorageRepository<ItemResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (!variant.hasCapacity()) {
            final TrackedStorageImpl<ItemResource> delegate = new TrackedStorageImpl<>(
                new InMemoryStorageImpl<>(),
                trackingRepository,
                System::currentTimeMillis
            );
            return new PlatformStorage<>(
                delegate,
                ItemStorageType.INSTANCE,
                trackingRepository,
                PlatformApi.INSTANCE.getStorageRepository(level)::markAsChanged
            );
        }
        final LimitedStorageImpl<ItemResource> delegate = new LimitedStorageImpl<>(
            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
            variant.getCapacity()
        );
        return new LimitedPlatformStorage<>(
            delegate,
            ItemStorageType.INSTANCE,
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
        if (variant == ItemStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getItemStoragePart(variant), count);
    }
}
