package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack;

import com.refinedmods.refinedstorage2.api.grid.view.stack.FluidGridStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public class FabricFluidGridStack extends FluidGridStack {
    private final FluidVariant platformStack;

    public FabricFluidGridStack(Rs2FluidStack stack, String name, String modId, String modName, Set<String> tags) {
        super(stack, name, modId, modName, tags);
        this.platformStack = Rs2PlatformApiFacade.INSTANCE.fluidResourceAmountConversion().toPlatform(stack).resource();
    }

    public FluidVariant getPlatformStack() {
        return platformStack;
    }
}
