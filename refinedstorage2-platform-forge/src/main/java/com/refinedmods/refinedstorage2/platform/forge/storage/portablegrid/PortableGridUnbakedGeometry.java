package com.refinedmods.refinedstorage2.platform.forge.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.forge.support.render.DiskModelBaker;
import com.refinedmods.refinedstorage2.platform.forge.support.render.RotationTranslationModelBaker;

import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class PortableGridUnbakedGeometry implements IUnbakedGeometry<PortableGridUnbakedGeometry> {
    private static final ResourceLocation ACTIVE_MODEL = createIdentifier("block/portable_grid/active");
    private static final ResourceLocation INACTIVE_MODEL = createIdentifier("block/portable_grid/inactive");
    private static final ResourceLocation INACTIVE_LED_MODEL = createIdentifier("block/disk/led_inactive");
    private static final ResourceLocation NORMAL_LED_MODEL = createIdentifier("block/disk/led_normal");
    private static final ResourceLocation NEAR_CAPACITY_LED_MODEL = createIdentifier("block/disk/led_near_capacity");
    private static final ResourceLocation FULL_LED_MODEL = createIdentifier("block/disk/led_full");

    PortableGridUnbakedGeometry() {
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        modelGetter.apply(ACTIVE_MODEL).resolveParents(modelGetter);
        modelGetter.apply(INACTIVE_MODEL).resolveParents(modelGetter);
        modelGetter.apply(INACTIVE_LED_MODEL).resolveParents(modelGetter);
        modelGetter.apply(NORMAL_LED_MODEL).resolveParents(modelGetter);
        modelGetter.apply(NEAR_CAPACITY_LED_MODEL).resolveParents(modelGetter);
        modelGetter.apply(FULL_LED_MODEL).resolveParents(modelGetter);
        PlatformApi.INSTANCE.getStorageContainerItemHelper().getDiskModels().forEach(
            diskModel -> modelGetter.apply(diskModel).resolveParents(modelGetter)
        );
    }

    @Override
    public BakedModel bake(final IGeometryBakingContext context,
                           final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState modelState,
                           final ItemOverrides overrides,
                           final ResourceLocation modelLocation) {
        return new PortableGridBakedModel(
            requireNonNull(baker.bake(INACTIVE_MODEL, modelState, spriteGetter)),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, ACTIVE_MODEL),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, INACTIVE_MODEL),
            new DiskModelBaker(modelState, baker, spriteGetter),
            new DiskLedBakers(
                new RotationTranslationModelBaker(modelState, baker, spriteGetter, INACTIVE_LED_MODEL),
                new RotationTranslationModelBaker(modelState, baker, spriteGetter, NORMAL_LED_MODEL),
                new RotationTranslationModelBaker(modelState, baker, spriteGetter, NEAR_CAPACITY_LED_MODEL),
                new RotationTranslationModelBaker(modelState, baker, spriteGetter, FULL_LED_MODEL)
            )
        );
    }
}
