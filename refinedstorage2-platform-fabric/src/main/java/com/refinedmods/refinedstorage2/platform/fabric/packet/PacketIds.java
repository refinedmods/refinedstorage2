package com.refinedmods.refinedstorage2.platform.fabric.packet;

import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public final class PacketIds {
    public static final ResourceLocation STORAGE_INFO_RESPONSE = createIdentifier("storage_info_response");
    public static final ResourceLocation STORAGE_INFO_REQUEST = createIdentifier("storage_info_request");
    public static final ResourceLocation CONTROLLER_ENERGY_INFO = createIdentifier("controller_energy");
    public static final ResourceLocation GRID_ACTIVE = createIdentifier("grid_active");
    public static final ResourceLocation GRID_UPDATE = createIdentifier("grid_update");
    public static final ResourceLocation PROPERTY_CHANGE = createIdentifier("property_change");
    public static final ResourceLocation GRID_INSERT = createIdentifier("grid_insert");
    public static final ResourceLocation GRID_EXTRACT = createIdentifier("grid_extract");
    public static final ResourceLocation GRID_SCROLL = createIdentifier("grid_scroll");
    public static final ResourceLocation RESOURCE_FILTER_SLOT_UPDATE = createIdentifier("resource_filter_slot_update");
    public static final ResourceLocation RESOURCE_FILTER_SLOT_CHANGE = createIdentifier("resource_filter_slot_change");
    public static final ResourceLocation RESOURCE_FILTER_SLOT_AMOUNT_CHANGE =
        createIdentifier("resource_filter_slot_amount_change");

    private PacketIds() {
    }
}
