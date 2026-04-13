package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.core.CoreValidations;

import java.util.UUID;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Represents a unique pattern, with an ID.
 *
 * @param id     the id
 * @param layout the (non-unique) layout
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.6")
public record Pattern(UUID id, PatternLayout layout) {
    public Pattern {
        CoreValidations.validateNotNull(id, "ID cannot be null");
        CoreValidations.validateNotNull(layout, "Layout cannot be null");
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return obj instanceof Pattern(UUID uuid, PatternLayout patternLayout)
            && uuid.equals(id)
            && patternLayout.equals(layout);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
