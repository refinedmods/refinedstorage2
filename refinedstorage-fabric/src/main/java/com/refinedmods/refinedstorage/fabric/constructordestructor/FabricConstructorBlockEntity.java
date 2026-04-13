package com.refinedmods.refinedstorage.fabric.constructordestructor;

import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FabricConstructorBlockEntity extends AbstractConstructorBlockEntity {
    public FabricConstructorBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}
