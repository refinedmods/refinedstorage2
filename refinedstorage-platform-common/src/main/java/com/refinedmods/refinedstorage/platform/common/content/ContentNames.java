package com.refinedmods.refinedstorage.platform.common.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public final class ContentNames {
    public static final String MOD_TRANSLATION_KEY = "mod." + MOD_ID;
    public static final MutableComponent MOD = Component.translatable(MOD_TRANSLATION_KEY);
    public static final MutableComponent CABLE = name("cable");
    public static final MutableComponent GRID = name("grid");
    public static final MutableComponent CRAFTING_GRID = name("crafting_grid");
    public static final MutableComponent DETECTOR = name("detector");
    public static final MutableComponent IMPORTER = name("importer");
    public static final MutableComponent EXPORTER = name("exporter");
    public static final MutableComponent EXTERNAL_STORAGE = name("external_storage");
    public static final MutableComponent CONSTRUCTOR = name("constructor");
    public static final MutableComponent DESTRUCTOR = name("destructor");
    public static final MutableComponent CONTROLLER = name("controller");
    public static final MutableComponent CREATIVE_CONTROLLER = name("creative_controller");
    public static final MutableComponent WIRELESS_GRID = createTranslation("item", "wireless_grid");
    public static final MutableComponent WIRELESS_TRANSMITTER = name("wireless_transmitter");
    public static final MutableComponent REGULATOR_UPGRADE = createTranslation("item", "regulator_upgrade");
    public static final MutableComponent STORAGE_MONITOR = name("storage_monitor");
    public static final MutableComponent INTERFACE = name("interface");
    public static final MutableComponent DISK_DRIVE = name("disk_drive");
    public static final MutableComponent NETWORK_RECEIVER = name("network_receiver");
    public static final MutableComponent NETWORK_TRANSMITTER = name("network_transmitter");
    public static final MutableComponent PORTABLE_GRID = name("portable_grid");
    public static final MutableComponent SECURITY_CARD = createTranslation("item", "security_card");
    public static final MutableComponent FALLBACK_SECURITY_CARD = createTranslation("item", "fallback_security_card");
    public static final MutableComponent SECURITY_MANAGER = name("security_manager");
    public static final MutableComponent RELAY = name("relay");
    public static final MutableComponent DISK_INTERFACE = name("disk_interface");

    private ContentNames() {
    }

    private static MutableComponent name(final String name) {
        return createTranslation("block", name);
    }
}
