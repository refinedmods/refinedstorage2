package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

class SimpleCablePart implements CablePart {
    private final Identifier model;

    SimpleCablePart(final Identifier model) {
        this.model = model;
    }

    @Override
    public ModelBaker.@Nullable SharedOperationKey<BlockStateModelPart> getKey(final BlockState state) {
        if (state.getBlock() instanceof AbstractDirectionalBlock<?> directionalBlock
            && directionalBlock.getDirection(state) instanceof Direction direction) {
            return new OperationKey(direction, model);
        }
        return null;
    }

    private record OperationKey(Direction direction, Identifier model)
        implements ModelBaker.SharedOperationKey<BlockStateModelPart> {
        @Override
        public BlockStateModelPart compute(final ModelBaker modelBaker) {
            return SimpleModelWrapper.bake(modelBaker, model, CableBlockStateModel.getRotation(direction));
        }
    }
}
