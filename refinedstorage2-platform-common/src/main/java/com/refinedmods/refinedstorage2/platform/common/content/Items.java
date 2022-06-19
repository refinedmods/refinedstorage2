package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageType.Variant, Supplier<ItemStoragePartItem>> itemStorageParts = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<FluidStoragePartItem>> fluidStorageParts = new EnumMap<>(FluidStorageType.Variant.class);
    private final List<Supplier<ControllerBlockItem>> controllers = new ArrayList<>();
    private Supplier<StorageHousingItem> storageHousing;

    private Items() {
    }

    public ItemStoragePartItem getItemStoragePart(ItemStorageType.Variant variant) {
        return itemStorageParts.get(variant).get();
    }

    public void setItemStoragePart(ItemStorageType.Variant variant, Supplier<ItemStoragePartItem> itemStoragePartSupplier) {
        itemStorageParts.put(variant, itemStoragePartSupplier);
    }

    public FluidStoragePartItem getFluidStoragePart(FluidStorageType.Variant type) {
        return fluidStorageParts.get(type).get();
    }

    public void setFluidStoragePart(FluidStorageType.Variant variant, Supplier<FluidStoragePartItem> fluidStoragePartSupplier) {
        fluidStorageParts.put(variant, fluidStoragePartSupplier);
    }

    public List<Supplier<ControllerBlockItem>> getControllers() {
        return controllers;
    }

    public StorageHousingItem getStorageHousing() {
        return storageHousing.get();
    }

    public void setStorageHousing(Supplier<StorageHousingItem> storageHousingSupplier) {
        this.storageHousing = storageHousingSupplier;
    }
}
