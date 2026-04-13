package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem.isValid;

public class PatternInventory extends FilteredContainer {
    @Nullable
    private Runnable listener;

    public PatternInventory(final int patterns, final Supplier<@Nullable Level> levelSupplier) {
        super(patterns, stack -> Optional.ofNullable(levelSupplier.get())
            .map(level -> isValid(stack, level))
            .orElse(false));
    }

    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (listener != null) {
            listener.run();
        }
    }

    public long getEnergyUsage() {
        long patterns = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            final ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                patterns++;
            }
        }
        return patterns * Platform.INSTANCE.getConfig().getAutocrafter().getEnergyUsagePerPattern();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(final ItemStack stack) {
        return 1;
    }
}
