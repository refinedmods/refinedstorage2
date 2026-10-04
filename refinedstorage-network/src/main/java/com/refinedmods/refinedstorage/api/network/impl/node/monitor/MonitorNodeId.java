package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import java.util.UUID;

import static com.refinedmods.refinedstorage.api.core.CoreValidations.validateNotNull;

public record MonitorNodeId(UUID id) {
    public MonitorNodeId {
        validateNotNull(id, "Id must not be null");
    }

    public static MonitorNodeId create() {
        return new MonitorNodeId(UUID.randomUUID());
    }
}
