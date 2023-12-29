package com.refinedmods.refinedstorage2.platform.forge.storage.portablegrid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class PortableGridGeometryLoader implements IGeometryLoader<PortableGridUnbakedGeometry> {
    @Override
    public PortableGridUnbakedGeometry read(final JsonObject jsonObject,
                                            final JsonDeserializationContext deserializationContext) {
        return new PortableGridUnbakedGeometry();
    }
}
