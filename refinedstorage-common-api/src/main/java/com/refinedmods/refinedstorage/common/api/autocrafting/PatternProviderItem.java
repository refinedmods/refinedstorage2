package com.refinedmods.refinedstorage.common.api.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.6")
public interface PatternProviderItem {
    static boolean isValid(final ItemStack stack, final Level level) {
        return stack.getItem() instanceof PatternProviderItem patternProviderItem
            && patternProviderItem.getPattern(stack, level).isPresent();
    }

    @Nullable
    UUID getId(ItemStack stack);

    Optional<Pattern> getPattern(ItemStack stack, Level level);

    Optional<ItemStack> getOutput(ItemStack stack, Level level);
}
