package com.refinedmods.refinedstorage2.platform.common.storage.storagedisk;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResourceRendering;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FluidStorageDiskItem extends AbstractStorageContainerItem {
    private final FluidStorageType.Variant variant;

    public FluidStorageDiskItem(final FluidStorageType.Variant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
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
        return FluidResourceRendering.format(amount);
    }

    @Override
    protected Storage createStorage(final StorageRepository storageRepository) {
        return StorageTypes.FLUID.create(variant.getCapacity(), storageRepository::markAsChanged);
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
