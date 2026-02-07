package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class PortableGridUnbakedBlockStateModel implements CustomUnbakedBlockStateModel {
    public static final MapCodec<PortableGridUnbakedBlockStateModel> MODEL_CODEC =
        MapCodec.unit(PortableGridUnbakedBlockStateModel::new);

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return MODEL_CODEC;
    }

    @Override
    public BlockStateModel bake(final ModelBaker modelBaker) {
        final ModelDebugName debugName = getClass()::toString;
        final Material.Baked particleMaterial = modelBaker.materials().get(
            new Material(createIdentifier("block/portable_grid/portable_grid_1")),
            debugName
        );
        return new PortableGridBlockStateModel(particleMaterial, modelBaker);
    }

    @Override
    public void resolveDependencies(final Resolver resolver) {
        resolver.markDependency(PortableGridRenderingProperties.ACTIVE_MODEL);
        resolver.markDependency(PortableGridRenderingProperties.INACTIVE_MODEL);
        RefinedStorageClientApi.INSTANCE.getDiskModels().forEach(resolver::markDependency);
    }
}
