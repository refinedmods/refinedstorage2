package com.refinedmods.refinedstorage2.platform.forge.support.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class DiskDriveGeometryLoader implements IGeometryLoader<DiskDriveUnbakedGeometry> {
    @Override
    public DiskDriveUnbakedGeometry read(final JsonObject jsonObject,
                                         final JsonDeserializationContext deserializationContext) {
        return new DiskDriveUnbakedGeometry();
    }
}
