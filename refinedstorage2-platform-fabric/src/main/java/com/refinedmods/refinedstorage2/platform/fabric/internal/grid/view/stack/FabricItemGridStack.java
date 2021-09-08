package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack;

import com.refinedmods.refinedstorage2.api.grid.view.stack.ItemGridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import java.util.Set;

import net.minecraft.item.ItemStack;

public class FabricItemGridStack extends ItemGridStack {
    private final ItemStack platformStack;

    public FabricItemGridStack(Rs2ItemStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
        this.platformStack = Rs2PlatformApiFacade.INSTANCE.itemStackConversion().toPlatform(stack);
    }

    public ItemStack getPlatformStack() {
        return platformStack;
    }
}
