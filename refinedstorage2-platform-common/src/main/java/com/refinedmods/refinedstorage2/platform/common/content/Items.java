package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageType.Variant, Supplier<Item>> itemStorageParts = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<Item>> fluidStorageParts = new EnumMap<>(FluidStorageType.Variant.class);
    private final List<Supplier<ControllerBlockItem>> controllers = new ArrayList<>();
    @Nullable
    private Supplier<Item> storageHousing;

    private Items() {
    }

    public Item getItemStoragePart(final ItemStorageType.Variant variant) {
        return itemStorageParts.get(variant).get();
    }

    public void setItemStoragePart(final ItemStorageType.Variant variant, final Supplier<Item> itemStoragePartSupplier) {
        itemStorageParts.put(variant, itemStoragePartSupplier);
    }

    public Item getFluidStoragePart(final FluidStorageType.Variant type) {
        return fluidStorageParts.get(type).get();
    }

    public void setFluidStoragePart(final FluidStorageType.Variant variant, final Supplier<Item> fluidStoragePartSupplier) {
        fluidStorageParts.put(variant, fluidStoragePartSupplier);
    }

    public List<Supplier<ControllerBlockItem>> getControllers() {
        return controllers;
    }

    public Item getStorageHousing() {
        return Objects.requireNonNull(storageHousing).get();
    }

    public void setStorageHousing(final Supplier<Item> storageHousingSupplier) {
        this.storageHousing = storageHousingSupplier;
    }
}
