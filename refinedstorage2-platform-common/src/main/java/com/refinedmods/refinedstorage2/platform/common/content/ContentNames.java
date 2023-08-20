package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public final class ContentNames {
    private static final String BLOCK_CATEGORY = "block";

    public static final MutableComponent CABLE = createTranslation(BLOCK_CATEGORY, "cable");
    public static final MutableComponent GRID = createTranslation(BLOCK_CATEGORY, "grid");
    public static final MutableComponent CRAFTING_GRID = createTranslation(BLOCK_CATEGORY, "crafting_grid");
    public static final MutableComponent DETECTOR = createTranslation(BLOCK_CATEGORY, "detector");
    public static final MutableComponent IMPORTER = createTranslation(BLOCK_CATEGORY, "importer");
    public static final MutableComponent EXPORTER = createTranslation(BLOCK_CATEGORY, "exporter");
    public static final MutableComponent EXTERNAL_STORAGE = createTranslation(BLOCK_CATEGORY, "external_storage");
    public static final MutableComponent CONSTRUCTOR = createTranslation(BLOCK_CATEGORY, "constructor");
    public static final MutableComponent DESTRUCTOR = createTranslation(BLOCK_CATEGORY, "destructor");
    public static final MutableComponent CONTROLLER = createTranslation(BLOCK_CATEGORY, "controller");
    public static final MutableComponent CREATIVE_CONTROLLER = createTranslation(BLOCK_CATEGORY, "creative_controller");
    public static final MutableComponent WIRELESS_GRID = createTranslation("item", "wireless_grid");

    private ContentNames() {
    }
}
