package com.refinedmods.refinedstorage2.platform.forge.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResourceFactory;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class ForgeItemGridResourceFactory extends ItemGridResourceFactory {
    @Override
    public Optional<String> getModName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName());
    }

    @Override
    public String getModId(ItemStack itemStack) {
        return itemStack.getItem().getCreatorModId(itemStack);
    }
}
