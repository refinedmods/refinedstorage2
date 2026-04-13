package com.refinedmods.refinedstorage.fabric.networking;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface CablePart {
    ModelBaker.@Nullable SharedOperationKey<BlockStateModelPart> getKey(BlockState state);
}
