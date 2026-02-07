package com.refinedmods.refinedstorage.fabric.storage.diskinterface;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class DiskInterfaceUnbakedBlockStateModel implements CustomUnbakedBlockStateModel {
    public static final MapCodec<DiskInterfaceUnbakedBlockStateModel> MODEL_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            DyeColor.CODEC.fieldOf("color").forGetter(unbaked -> unbaked.color)
        ).apply(instance, DiskInterfaceUnbakedBlockStateModel::new));

    private final DyeColor color;

    public DiskInterfaceUnbakedBlockStateModel(final DyeColor color) {
        this.color = color;
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return MODEL_CODEC;
    }

    @Override
    public BlockStateModel bake(final ModelBaker modelBaker) {
        final ModelDebugName debugName = getClass()::toString;
        final Material.Baked particleMaterial = modelBaker.materials().get(
            new Material(createIdentifier("block/disk_interface/top")),
            debugName
        );
        return new DiskInterfaceBlockStateModel(particleMaterial, modelBaker, color);
    }

    @Override
    public void resolveDependencies(final Resolver resolver) {
        resolver.markDependency(DiskInterfaceRenderingProperties.INACTIVE_BASE_MODEL);
        resolver.markDependency(DiskInterfaceRenderingProperties.getActiveBaseModel(color));
        resolver.markDependency(DiskInterfaceRenderingProperties.INACTIVE_LED_MODEL);
        RefinedStorageClientApi.INSTANCE.getDiskModels().forEach(resolver::markDependency);
    }
}
