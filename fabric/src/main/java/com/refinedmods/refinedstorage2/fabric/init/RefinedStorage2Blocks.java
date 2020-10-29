package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Blocks {
    private final CableBlock cable = new CableBlock();

    public void register(String namespace) {
        Registry.register(Registry.BLOCK, new Identifier(namespace, "cable"), cable);
    }

    public CableBlock getCable() {
        return cable;
    }
}
