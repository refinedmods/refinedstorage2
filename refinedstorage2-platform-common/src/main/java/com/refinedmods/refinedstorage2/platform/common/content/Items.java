package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageType.Variant, Supplier<StoragePartItem>> storageParts = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<FluidStoragePartItem>> fluidStorageParts = new EnumMap<>(FluidStorageType.Variant.class);
    private final List<Supplier<ControllerBlockItem>> controllers = new ArrayList<>();
    private Supplier<StorageHousingItem> storageHousing;

    private Items() {
    }

    public StoragePartItem getStoragePart(ItemStorageType.Variant variant) {
        return storageParts.get(variant).get();
    }

    public FluidStoragePartItem getFluidStoragePart(FluidStorageType.Variant type) {
        return fluidStorageParts.get(type).get();
    }

    public Map<ItemStorageType.Variant, Supplier<StoragePartItem>> getStorageParts() {
        return storageParts;
    }

    public Map<FluidStorageType.Variant, Supplier<FluidStoragePartItem>> getFluidStorageParts() {
        return fluidStorageParts;
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
