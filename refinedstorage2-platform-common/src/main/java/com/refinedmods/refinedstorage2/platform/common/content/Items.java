package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageDiskItem.ItemStorageType, StoragePartItem> storageParts = new EnumMap<>(ItemStorageDiskItem.ItemStorageType.class);
    private final Map<FluidStorageDiskItem.FluidStorageType, FluidStoragePartItem> fluidStorageParts = new EnumMap<>(FluidStorageDiskItem.FluidStorageType.class);
    private final List<ControllerBlockItem> controllers = new ArrayList<>();
    private StorageHousingItem storageHousing;

    private Items() {
    }

    public StoragePartItem getStoragePart(ItemStorageDiskItem.ItemStorageType type) {
        return storageParts.get(type);
    }

    public FluidStoragePartItem getFluidStoragePart(FluidStorageDiskItem.FluidStorageType type) {
        return fluidStorageParts.get(type);
    }

    public Map<ItemStorageDiskItem.ItemStorageType, StoragePartItem> getStorageParts() {
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
