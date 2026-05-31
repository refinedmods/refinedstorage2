package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkDetails;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record AutocrafterExternalPatternSinkDetails(String name, ItemStack stack)
    implements ExternalPatternSinkDetails {

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final AutocrafterExternalPatternSinkDetails that = (AutocrafterExternalPatternSinkDetails) other;
        return Objects.equals(name, that.name) && ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name) * 31 + ItemStack.hashItemAndComponents(stack);
    }
}
