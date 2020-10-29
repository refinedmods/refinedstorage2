package com.refinedmods.refinedstorage2.fabric.item;

import net.minecraft.item.Item;

public class ProcessorItem extends Item {
    public enum Type {
        RAW_BASIC("raw_basic"),
        RAW_IMPROVED("raw_improved"),
        RAW_ADVANCED("raw_advanced"),
        BASIC("basic"),
        IMPROVED("improved"),
        ADVANCED("advanced");

        final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ProcessorItem(Settings settings) {
        super(settings);
    }
}
