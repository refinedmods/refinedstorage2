package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageType.Variant, StoragePartItem> storageParts = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageDiskItem.FluidStorageType, FluidStoragePartItem> fluidStorageParts = new EnumMap<>(FluidStorageDiskItem.FluidStorageType.class);
    private final List<ControllerBlockItem> controllers = new ArrayList<>();
    private StorageHousingItem storageHousing;

    private Items() {
    }

    public StoragePartItem getStoragePart(ItemStorageType.Variant variant) {
        return storageParts.get(variant);
    }

    public FluidStoragePartItem getFluidStoragePart(FluidStorageDiskItem.FluidStorageType type) {
        return fluidStorageParts.get(type);
    }

    public Map<ItemStorageType.Variant, StoragePartItem> getStorageParts() {
        return storageParts;
    }

    public Map<FluidStorageDiskItem.FluidStorageType, FluidStoragePartItem> getFluidStorageParts() {
        return fluidStorageParts;
    }

    public List<ControllerBlockItem> getControllers() {
        return controllers;
    }

    public StorageHousingItem getStorageHousing() {
        return storageHousing;
    }

    public void setStorageHousing(StorageHousingItem storageHousing) {
        this.storageHousing = storageHousing;
    }
}
