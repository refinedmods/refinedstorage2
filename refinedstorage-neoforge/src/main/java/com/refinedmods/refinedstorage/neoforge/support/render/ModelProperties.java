package com.refinedmods.refinedstorage.neoforge.support.render;

import com.refinedmods.refinedstorage.common.networking.CableConnections;

import net.neoforged.neoforge.model.data.ModelProperty;

public final class ModelProperties {
    public static final ModelProperty<CableConnections> CABLE_CONNECTIONS = new ModelProperty<>();

    private ModelProperties() {
    }
}
