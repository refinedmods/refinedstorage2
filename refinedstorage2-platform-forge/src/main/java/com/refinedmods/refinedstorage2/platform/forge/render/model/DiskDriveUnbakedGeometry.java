package com.refinedmods.refinedstorage2.platform.forge.render.model;

import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;
import com.refinedmods.refinedstorage2.platform.forge.render.model.baked.DiskDriveBakedModel;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DiskDriveUnbakedGeometry extends AbstractUnbakedGeometry<DiskDriveUnbakedGeometry> {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive_base");
    private static final ResourceLocation DISK_INACTIVE_MODEL = createIdentifier("block/disk_inactive");
    private static final ResourceLocation DISK_MODEL = createIdentifier("block/disk");

    @Override
    protected Set<ResourceLocation> getModels() {
        return Set.of(BASE_MODEL, DISK_MODEL);
    }

    @Override
    public BakedModel bake(final IGeometryBakingContext context,
                           final ModelBakery bakery,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState modelState,
                           final ItemOverrides overrides,
                           final ResourceLocation modelLocation) {
        return new DiskDriveBakedModel(
            getBaseModelBakery(modelState, bakery, spriteGetter),
            Objects.requireNonNull(bakery.bake(BASE_MODEL, modelState, spriteGetter)),
            getDiskModelBakery(modelState, bakery, spriteGetter),
            getDiskItemModelBakery(modelState, bakery, spriteGetter)
        );
    }

    private Function<BiDirection, BakedModel> getBaseModelBakery(final ModelState state,
                                                                 final ModelBakery bakery,
                                                                 final Function<Material, TextureAtlasSprite> sg) {
        return direction -> {
            final Transformation rotation = new Transformation(null, direction.getQuaternion(), null, null);
            final ModelState wrappedState = new SimpleModelState(rotation, state.isUvLocked());
            return bakery.bake(BASE_MODEL, wrappedState, sg);
        };
    }

    private BiFunction<BiDirection, Vector3f, BakedModel> getDiskModelBakery(final ModelState state,
                                                                             final ModelBakery bakery,
                                                                             final Function
                                                                                 <Material, TextureAtlasSprite> sg) {
        return (direction, trans) -> {
            final Transformation translation = new Transformation(trans, null, null, null);
            final Transformation rotation = new Transformation(null, direction.getQuaternion(), null, null);
            final ModelState wrappedState = new SimpleModelState(rotation.compose(translation), state.isUvLocked());
            return bakery.bake(DISK_MODEL, wrappedState, sg);
        };
    }

    private Function<Vector3f, BakedModel> getDiskItemModelBakery(final ModelState state,
                                                                  final ModelBakery bakery,
                                                                  final Function<Material, TextureAtlasSprite>
                                                                      sg) {
        return trans -> {
            final Transformation translation = new Transformation(trans, null, null, null);
            final ModelState wrappedState = new SimpleModelState(translation, state.isUvLocked());
            return bakery.bake(DISK_INACTIVE_MODEL, wrappedState, sg);
        };
    }
}
