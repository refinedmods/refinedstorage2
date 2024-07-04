package com.refinedmods.refinedstorage.platform.common.storage;

import java.util.List;
import java.util.function.LongFunction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

final class StorageTooltipHelper {
    private StorageTooltipHelper() {
    }

    static void addAmountStoredWithoutCapacity(final List<Component> tooltip,
                                               final long stored,
                                               final LongFunction<String> quantityFormatter) {
        tooltip.add(createTranslation(
            "misc",
            "stored",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));
    }

    static void addAmountStoredWithCapacity(final List<Component> tooltip,
                                            final long stored,
                                            final long capacity,
                                            final LongFunction<String> quantityFormatter) {
        if (capacity <= 0) {
            addAmountStoredWithoutCapacity(tooltip, stored, quantityFormatter);
            return;
        }
        final int progress = (int) ((double) stored / capacity * 100D);
        tooltip.add(createTranslation(
            "misc",
            "stored_with_capacity",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.WHITE),
            Component.literal(quantityFormatter.apply(capacity)).withStyle(ChatFormatting.WHITE),
            Component.literal(String.valueOf(progress))
        ).withStyle(ChatFormatting.GRAY));
    }
}
