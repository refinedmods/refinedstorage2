package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.StorageTypes;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStorageDiskItem extends AbstractStorageContainerItem<ItemResource> {
    private final ItemStorageType.Variant variant;

    public ItemStorageDiskItem(final ItemStorageType.Variant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
            StorageChannelTypes.ITEM,
            PlatformApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
    }

    @Override
    protected boolean hasCapacity() {
        return variant.hasCapacity();
    }

    @Override
    protected String formatAmount(final long amount) {
        return AmountFormatting.format(amount);
    }

    @Override
    protected Storage<ItemResource> createStorage(final StorageRepository storageRepository) {
        return StorageTypes.ITEM.create(variant.getCapacity(), storageRepository::markAsChanged);
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
