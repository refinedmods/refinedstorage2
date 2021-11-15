package com.refinedmods.refinedstorage2.platform.fabric.packet;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.resources.ResourceLocation;

public final class PacketIds {
    public static final ResourceLocation STORAGE_INFO_RESPONSE = Rs2Mod.createIdentifier("storage_info_response");
    public static final ResourceLocation STORAGE_INFO_REQUEST = Rs2Mod.createIdentifier("storage_info_request");
    public static final ResourceLocation CONTROLLER_ENERGY = Rs2Mod.createIdentifier("controller_energy");
    public static final ResourceLocation GRID_ACTIVE = Rs2Mod.createIdentifier("grid_active");
    public static final ResourceLocation GRID_ITEM_UPDATE = Rs2Mod.createIdentifier("grid_item_update");
    public static final ResourceLocation GRID_FLUID_UPDATE = Rs2Mod.createIdentifier("grid_fluid_update");
    public static final ResourceLocation PROPERTY_CHANGE = Rs2Mod.createIdentifier("property_change");
    public static final ResourceLocation GRID_INSERT = Rs2Mod.createIdentifier("grid_insert");
    public static final ResourceLocation GRID_EXTRACT = Rs2Mod.createIdentifier("grid_extract");
    public static final ResourceLocation GRID_SCROLL = Rs2Mod.createIdentifier("grid_scroll");
    public static final ResourceLocation RESOURCE_FILTER_SLOT_UPDATE = Rs2Mod.createIdentifier("resource_filter_slot_update");
    public static final ResourceLocation RESOURCE_TYPE_CHANGE = Rs2Mod.createIdentifier("resource_type_change");

    private PacketIds() {
    }
}
