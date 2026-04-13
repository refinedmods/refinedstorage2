package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public enum PatternType implements StringRepresentable {
    CRAFTING("crafting"),
    PROCESSING("processing"),
    STONECUTTER("stonecutter"),
    SMITHING_TABLE("smithing_table");

    public static final Codec<PatternType> CODEC = StringRepresentable.fromValues(PatternType::values);

    private final String name;
    private final Component translatedName;

    PatternType(final String name) {
        this.name = name;
        this.translatedName = createTranslation("misc", "pattern." + name);
    }

    public Component getTranslatedName() {
        return translatedName;
    }

    PatternGridRenderer createRenderer(final PatternGridContainerMenu menu,
                                       final int leftPos,
                                       final int topPos,
                                       final int x,
                                       final int y) {
        return switch (this) {
            case CRAFTING -> new CraftingPatternGridRenderer(menu, leftPos, x, y);
            case PROCESSING -> new ProcessingPatternGridRenderer(menu, leftPos, topPos, x, y);
            case STONECUTTER -> new StonecutterPatternGridRenderer(menu, leftPos, x, y);
            case SMITHING_TABLE -> new SmithingTablePatternGridRenderer(menu, leftPos, topPos, x, y);
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
