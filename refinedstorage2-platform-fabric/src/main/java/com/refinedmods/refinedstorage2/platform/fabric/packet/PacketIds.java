package com.refinedmods.refinedstorage2.platform.fabric.packet;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.minecraft.util.Identifier;

public final class PacketIds {
    public static final Identifier STORAGE_DISK_INFO_RESPONSE = Rs2Mod.createIdentifier("storage_disk_info_response");
    public static final Identifier STORAGE_DISK_INFO_REQUEST = Rs2Mod.createIdentifier("storage_disk_info_request");
    public static final Identifier CONTROLLER_ENERGY = Rs2Mod.createIdentifier("controller_energy");
    public static final Identifier GRID_ACTIVE = Rs2Mod.createIdentifier("grid_active");
    public static final Identifier GRID_ITEM_UPDATE = Rs2Mod.createIdentifier("grid_item_update");
    public static final Identifier GRID_FLUID_UPDATE = Rs2Mod.createIdentifier("grid_fluid_update");
    public static final Identifier PROPERTY_CHANGE = Rs2Mod.createIdentifier("property_change");
    public static final Identifier GRID_INSERT = Rs2Mod.createIdentifier("grid_insert");

    private PacketIds() {
    }
}
