package com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DiskDriveUnbakedGeometry implements IUnbakedGeometry<DiskDriveUnbakedGeometry> {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive/base");
    private static final ResourceLocation LED_INACTIVE_MODEL = createIdentifier("block/disk/led_inactive");

    DiskDriveUnbakedGeometry() {
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        modelGetter.apply(BASE_MODEL).resolveParents(modelGetter);
        PlatformApi.INSTANCE.getStorageContainerItemHelper().getDiskModels().forEach(
            diskModel -> modelGetter.apply(diskModel).resolveParents(modelGetter)
        );
        modelGetter.apply(LED_INACTIVE_MODEL).resolveParents(modelGetter);
    }

    @Override
    public BakedModel bake(final IGeometryBakingContext context,
                           final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState modelState,
                           final ItemOverrides overrides,
                           final ResourceLocation modelLocation) {
        final var diskModelBakeries = PlatformApi.INSTANCE.getStorageContainerItemHelper()
            .getDiskModelsByItem()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> getDiskModelBakery(modelState, baker, spriteGetter, entry.getValue())
            ));
        final var diskItemModelBakeries = PlatformApi.INSTANCE.getStorageContainerItemHelper()
            .getDiskModelsByItem()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> getItemModelBakery(modelState, baker, spriteGetter, entry.getValue())
            ));
        return new DiskDriveBakedModel(
            getBaseModelBakery(modelState, baker, spriteGetter),
            Objects.requireNonNull(baker.bake(BASE_MODEL, modelState, spriteGetter)),
            diskModelBakeries,
            diskItemModelBakeries,
            getItemModelBakery(modelState, baker, spriteGetter, LED_INACTIVE_MODEL)
        );
    }

    private Function<BiDirection, BakedModel> getBaseModelBakery(final ModelState state,
                                                                 final ModelBaker baker,
                                                                 final Function<Material, TextureAtlasSprite> sg) {
        return direction -> {
            final Transformation rotation = new Transformation(null, direction.getQuaternion(), null, null);
            final ModelState wrappedState = new SimpleModelState(rotation, state.isUvLocked());
            return baker.bake(BASE_MODEL, wrappedState, sg);
        };
    }

    private BiFunction<BiDirection, Vector3f, BakedModel> getDiskModelBakery(final ModelState state,
                                                                             final ModelBaker baker,
                                                                             final Function
                                                                                 <Material, TextureAtlasSprite> sg,
                                                                             final ResourceLocation model) {
        return (direction, trans) -> {
            final Transformation translation = new Transformation(trans, null, null, null);
            final Transformation rotation = new Transformation(null, direction.getQuaternion(), null, null);
            final ModelState wrappedState = new SimpleModelState(rotation.compose(translation), state.isUvLocked());
            return baker.bake(model, wrappedState, sg);
        };
    }

    private Function<Vector3f, BakedModel> getItemModelBakery(final ModelState state,
                                                              final ModelBaker baker,
                                                              final Function<Material, TextureAtlasSprite> sg,
                                                              final ResourceLocation model) {
        return trans -> {
            final Transformation translation = new Transformation(trans, null, null, null);
            final ModelState wrappedState = new SimpleModelState(translation, state.isUvLocked());
            return baker.bake(model, wrappedState, sg);
        };
    }
}
