package com.refinedmods.refinedstorage.api.autocrafting.task;

import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.3")
public record ExternalPatternSinkId(UUID id) {
    public static ExternalPatternSinkId create() {
        return new ExternalPatternSinkId(UUID.randomUUID());
    }
}
