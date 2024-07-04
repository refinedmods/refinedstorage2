package com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class DiskInterfaceGeometryLoader implements IGeometryLoader<DiskInterfaceUnbakedGeometry> {
    private final DyeColor color;

    public DiskInterfaceGeometryLoader(final DyeColor color) {
        this.color = color;
    }

    @Override
    public DiskInterfaceUnbakedGeometry read(final JsonObject jsonObject,
                                             final JsonDeserializationContext deserializationContext) {
        return new DiskInterfaceUnbakedGeometry(color);
    }
}
