package com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive;

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
        return new DiskDriveBakedModel(
            requireNonNull(baker.bake(BASE_MODEL, modelState, spriteGetter)),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, BASE_MODEL),
            new DiskModelBaker(modelState, baker, spriteGetter),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, LED_INACTIVE_MODEL)
        );
    }
}
