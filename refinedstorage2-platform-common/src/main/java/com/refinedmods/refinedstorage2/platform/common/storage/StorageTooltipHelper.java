package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.List;
import java.util.function.LongFunction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class StorageTooltipHelper {
    private StorageTooltipHelper() {
    }

    public static void addAmountStoredWithoutCapacity(final List<Component> tooltip,
                                                      final long stored,
                                                      final LongFunction<String> quantityFormatter) {
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stored",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));
    }

    public static void addAmountStoredWithCapacity(final List<Component> tooltip,
                                                   final long stored,
                                                   final long capacity,
                                                   final LongFunction<String> quantityFormatter) {
        if (capacity <= 0) {
            addAmountStoredWithoutCapacity(tooltip, stored, quantityFormatter);
            return;
        }
        final int progress = (int) ((double) stored / capacity * 100D);
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stored_with_capacity",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.WHITE),
            Component.literal(quantityFormatter.apply(capacity)).withStyle(ChatFormatting.WHITE),
            Component.literal(String.valueOf(progress))
        ).withStyle(ChatFormatting.GRAY));
    }
}
