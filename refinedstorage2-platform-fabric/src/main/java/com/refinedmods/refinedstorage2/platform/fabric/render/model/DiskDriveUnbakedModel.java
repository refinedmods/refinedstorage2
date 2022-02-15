package com.refinedmods.refinedstorage2.platform.fabric.render.model;

import com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.DiskDriveBakedModel;

import java.util.Collection;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DiskDriveUnbakedModel implements BasicUnbakedModel {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive_base");
    private static final ResourceLocation DISK_DISCONNECTED_MODEL = createIdentifier("block/disk_disconnected");
    private static final ResourceLocation DISK_MODEL = createIdentifier("block/disk");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ImmutableSet.Builder<ResourceLocation>()
                .add(BASE_MODEL)
                .add(DISK_MODEL)
                .build();
    }

    @Override
    public @Nullable BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
        return new DiskDriveBakedModel(
                loader.bake(BASE_MODEL, rotationContainer),
                loader.bake(DISK_MODEL, rotationContainer),
                loader.bake(DISK_DISCONNECTED_MODEL, rotationContainer)
        );
    }
}
