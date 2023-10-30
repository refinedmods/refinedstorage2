package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public final class ContentNames {
    public static final MutableComponent CABLE = createTranslation("block", "cable");
    public static final MutableComponent GRID = createTranslation("block", "grid");
    public static final MutableComponent CRAFTING_GRID = createTranslation("block", "crafting_grid");
    public static final MutableComponent DETECTOR = createTranslation("block", "detector");
    public static final MutableComponent IMPORTER = createTranslation("block", "importer");
    public static final MutableComponent EXPORTER = createTranslation("block", "exporter");
    public static final MutableComponent EXTERNAL_STORAGE = createTranslation("block", "external_storage");
    public static final MutableComponent CONSTRUCTOR = createTranslation("block", "constructor");
    public static final MutableComponent DESTRUCTOR = createTranslation("block", "destructor");
    public static final MutableComponent CONTROLLER = createTranslation("block", "controller");
    public static final MutableComponent CREATIVE_CONTROLLER = createTranslation("block", "creative_controller");
    public static final MutableComponent WIRELESS_GRID = createTranslation("item", "wireless_grid");
    public static final MutableComponent WIRELESS_TRANSMITTER = createTranslation("block", "wireless_transmitter");
    public static final MutableComponent REGULATOR_UPGRADE = createTranslation("block", "regulator_upgrade");
    public static final MutableComponent STORAGE_MONITOR = createTranslation("block", "storage_monitor");
    public static final MutableComponent INTERFACE = createTranslation("block", "interface");
    public static final MutableComponent DISK_DRIVE = createTranslation("block", "disk_drive");

    private ContentNames() {
    }
}
