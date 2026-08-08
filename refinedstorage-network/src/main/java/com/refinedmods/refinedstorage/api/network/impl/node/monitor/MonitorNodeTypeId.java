package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import java.util.UUID;

import static com.refinedmods.refinedstorage.api.core.CoreValidations.validateNotNull;

public record MonitorNodeTypeId(UUID id) {
    public MonitorNodeTypeId {
        validateNotNull(id, "Id must not be null");
    }

    public static MonitorNodeTypeId create() {
        return new MonitorNodeTypeId(UUID.randomUUID());
    }
}
