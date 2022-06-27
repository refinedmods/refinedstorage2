package com.refinedmods.refinedstorage2.platform.forge.render.model;

import com.refinedmods.refinedstorage2.platform.forge.render.model.baked.DiskDriveBakedModel;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DiskDriveModelGeometry extends BasicModelGeometry<DiskDriveModelGeometry> {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive_base");
    private static final ResourceLocation DISK_DISCONNECTED_MODEL = createIdentifier("block/disk_disconnected");
    private static final ResourceLocation DISK_MODEL = createIdentifier("block/disk");

    @Override
    protected Set<ResourceLocation> getModels() {
        return Set.of(BASE_MODEL, DISK_MODEL);
    }

    @Override
    public BakedModel bake(final IModelConfiguration owner, final ModelBakery bakery, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform, final ItemOverrides overrides, final ResourceLocation modelLocation) {
        return new DiskDriveBakedModel(
                Objects.requireNonNull(bakery.bake(BASE_MODEL, modelTransform, spriteGetter)),
                Objects.requireNonNull(bakery.bake(DISK_MODEL, modelTransform, spriteGetter)),
                Objects.requireNonNull(bakery.bake(DISK_DISCONNECTED_MODEL, modelTransform, spriteGetter))
        );
    }
}
