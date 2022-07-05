package com.refinedmods.refinedstorage2.platform.forge.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.AbstractItemGridResourceFactory;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class ForgeItemGridResourceFactory extends AbstractItemGridResourceFactory {
    @Override
    public Optional<String> getModName(final String modId) {
        return ModList.get()
            .getModContainerById(modId)
            .map(container -> container.getModInfo().getDisplayName());
    }

    @Override
    public String getModId(final ItemStack itemStack) {
        return Objects.requireNonNullElse(itemStack.getItem().getCreatorModId(itemStack), "");
    }
}
