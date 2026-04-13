package com.refinedmods.refinedstorage.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class DiskDriveUnbakedBlockStateModel implements CustomUnbakedBlockStateModel {
    public static final MapCodec<DiskDriveUnbakedBlockStateModel> MODEL_CODEC = MapCodec
        .unit(DiskDriveUnbakedBlockStateModel::new);

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return MODEL_CODEC;
    }

    @Override
    public BlockStateModel bake(final ModelBaker modelBaker) {
        final ModelDebugName debugName = getClass()::toString;
        final Material.Baked particleMaterial = modelBaker.materials().get(
            new Material(createIdentifier("block/disk_drive/top")),
            debugName
        );
        return new DiskDriveBlockStateModel(particleMaterial, modelBaker);
    }

    @Override
    public void resolveDependencies(final Resolver resolver) {
        resolver.markDependency(DiskDriveRenderingProperties.BASE_MODEL);
        resolver.markDependency(DiskDriveRenderingProperties.INACTIVE_LED_MODEL);
        RefinedStorageClientApi.INSTANCE.getDiskModels().forEach(resolver::markDependency);
    }
}
