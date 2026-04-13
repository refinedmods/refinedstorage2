package com.refinedmods.refinedstorage.common.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ProcessorItem extends Item {
    public ProcessorItem(final Identifier id) {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
    }

    public enum Type {
        RAW_BASIC("raw_basic"),
        RAW_IMPROVED("raw_improved"),
        RAW_ADVANCED("raw_advanced"),
        BASIC("basic"),
        IMPROVED("improved"),
        ADVANCED("advanced");

        final String name;

        Type(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
