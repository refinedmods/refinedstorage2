package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public final class Tags {
    public static final TagKey<Item> CABLES = createTag("cables");
    public static final TagKey<Item> CONTROLLERS = createTag("controllers");
    public static final TagKey<Item> CREATIVE_CONTROLLERS = createTag("creative_controllers");
    public static final TagKey<Item> FLUID_STORAGE_DISKS = createTag("fluid_storage_disks");
    public static final TagKey<Item> GRIDS = createTag("grids");
    public static final TagKey<Item> CRAFTING_GRIDS = createTag("crafting_grids");
    public static final TagKey<Item> STORAGE_DISKS = createTag("storage_disks");
    public static final TagKey<Item> IMPORTERS = createTag("importers");
    public static final TagKey<Item> EXPORTERS = createTag("exporters");
    public static final TagKey<Item> EXTERNAL_STORAGES = createTag("external_storages");
    public static final TagKey<Item> DETECTORS = createTag("detectors");
    public static final TagKey<Item> DESTRUCTORS = createTag("destructors");

    private Tags() {
    }

    private static TagKey<Item> createTag(final String id) {
        return TagKey.create(Registries.ITEM, createIdentifier(id));
    }
}
