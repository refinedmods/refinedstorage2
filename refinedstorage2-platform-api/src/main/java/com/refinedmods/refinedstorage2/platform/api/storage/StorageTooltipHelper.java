package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class StorageTooltipHelper {
    private StorageTooltipHelper() {
    }

    public enum TooltipOption {
        CAPACITY_AND_PROGRESS,
        STACK_INFO
    }

    public static void appendToTooltip(List<Component> tooltip, long stored, long capacity, LongFunction<String> quantityFormatter, LongFunction<String> stackInfoQuantityFormatter, Set<TooltipOption> options) {
        if (!options.contains(TooltipOption.CAPACITY_AND_PROGRESS)) {
            addAmountStoredWithoutCapacity(tooltip, stored, quantityFormatter);
        } else {
            addAmountStoredWithCapacity(tooltip, stored, capacity, quantityFormatter);
        }
        if (options.contains(TooltipOption.STACK_INFO)) {
            if (!options.contains(TooltipOption.CAPACITY_AND_PROGRESS)) {
                addAmountOfStacksWithoutCapacity(tooltip, stackInfoQuantityFormatter, stored);
            } else {
                addAmountOfStacksWithCapacity(tooltip, stackInfoQuantityFormatter, stored, capacity);
            }
        }
        if (options.contains(TooltipOption.CAPACITY_AND_PROGRESS)) {
            addProgress(tooltip, stored, capacity);
        }
    }

    private static void addAmountStoredWithoutCapacity(List<Component> tooltip, long stored, LongFunction<String> quantityFormatter) {
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
                "misc",
                "stored",
                new TextComponent(quantityFormatter.apply(stored)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountStoredWithCapacity(List<Component> tooltip, long stored, long capacity, LongFunction<String> quantityFormatter) {
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
                "misc",
                "stored_with_capacity",
                new TextComponent(quantityFormatter.apply(stored)).withStyle(ChatFormatting.GREEN),
                new TextComponent(quantityFormatter.apply(capacity)).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountOfStacksWithoutCapacity(List<Component> tooltip, LongFunction<String> quantityFormatter, long stored) {
        long stacks = stored / 64L;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
                "misc",
                "stacks",
                new TextComponent(quantityFormatter.apply(stacks)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addAmountOfStacksWithCapacity(List<Component> tooltip, LongFunction<String> quantityFormatter, long stored, long capacity) {
        long stacks = stored / 64L;
        long maxStacks = capacity / 64L;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
                "misc",
                "stacks_with_capacity",
                new TextComponent(quantityFormatter.apply(stacks)).withStyle(ChatFormatting.GREEN),
                new TextComponent(quantityFormatter.apply(maxStacks)).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addProgress(List<Component> tooltip, double stored, double capacity) {
        double progress = stored / capacity;
        tooltip.add(PlatformApi.INSTANCE.createTranslation(
                "misc",
                "full",
                new TextComponent("" + (int) (progress * 100D)).withStyle(ChatFormatting.AQUA)
        ).withStyle(ChatFormatting.GRAY));
    }
}
