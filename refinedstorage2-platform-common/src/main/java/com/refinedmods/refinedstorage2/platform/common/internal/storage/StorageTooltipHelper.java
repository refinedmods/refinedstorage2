package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.List;
import java.util.function.LongFunction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class StorageTooltipHelper {
    private StorageTooltipHelper() {
    }

    public static void appendToTooltip(final List<Component> tooltip,
                                       final long stored,
                                       final long capacity,
                                       final LongFunction<String> quantityFormatter,
                                       final LongFunction<String> stackInfoQuantityFormatter,
                                       final boolean showCapacityAndProgress,
                                       final boolean showStackingInfo) {
        if (!showCapacityAndProgress) {
            addAmountStoredWithoutCapacity(tooltip, stored, quantityFormatter);
        } else {
            addAmountStoredWithCapacity(tooltip, stored, capacity, quantityFormatter);
        }
        if (showStackingInfo) {
            if (!showCapacityAndProgress) {
                addAmountOfStacksWithoutCapacity(tooltip, stackInfoQuantityFormatter, stored);
            } else {
                addAmountOfStacksWithCapacity(tooltip, stackInfoQuantityFormatter, stored, capacity);
            }
        }
        if (showCapacityAndProgress) {
            addProgress(tooltip, stored, capacity);
        }
    }

    private static void addAmountStoredWithoutCapacity(final List<Component> tooltip,
                                                       final long stored,
                                                       final LongFunction<String> quantityFormatter) {
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stored",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountStoredWithCapacity(final List<Component> tooltip,
                                                    final long stored,
                                                    final long capacity,
                                                    final LongFunction<String> quantityFormatter) {
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stored_with_capacity",
            Component.literal(quantityFormatter.apply(stored)).withStyle(ChatFormatting.GREEN),
            Component.literal(quantityFormatter.apply(capacity)).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountOfStacksWithoutCapacity(final List<Component> tooltip,
                                                         final LongFunction<String> quantityFormatter,
                                                         final long stored) {
        final long stacks = stored / 64L;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stacks",
            Component.literal(quantityFormatter.apply(stacks)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountOfStacksWithCapacity(final List<Component> tooltip,
                                                      final LongFunction<String> quantityFormatter,
                                                      final long stored,
                                                      final long capacity) {
        final long stacks = stored / 64L;
        final long maxStacks = capacity / 64L;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "stacks_with_capacity",
            Component.literal(quantityFormatter.apply(stacks)).withStyle(ChatFormatting.GREEN),
            Component.literal(quantityFormatter.apply(maxStacks)).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addProgress(final List<Component> tooltip,
                                    final double stored,
                                    final double capacity) {
        final double progress = stored / capacity;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
            "misc",
            "full",
            Component.literal("" + (int) (progress * 100D)).withStyle(ChatFormatting.AQUA)
        ).withStyle(ChatFormatting.GRAY));
    }
}