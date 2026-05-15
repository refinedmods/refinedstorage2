package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.core.CoreValidations;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record AutocrafterExternalPatternSinkKey(
    UUID id,
    @Nullable String name,
    @Nullable ItemStack stack
) implements ExternalPatternSinkKey {
    public AutocrafterExternalPatternSinkKey {
        CoreValidations.validateNotNull(id, "ID can not be null");
    }

    public static AutocrafterExternalPatternSinkKey create(final UUID id) {
        return new AutocrafterExternalPatternSinkKey(id, null, null);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AutocrafterExternalPatternSinkKey that = (AutocrafterExternalPatternSinkKey) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
