package com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.DiskModelBaker;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.RotationTranslationModelBaker;

import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class DiskInterfaceUnbakedGeometry implements IUnbakedGeometry<DiskInterfaceUnbakedGeometry> {
    private static final ResourceLocation INACTIVE_MODEL = createIdentifier("block/disk_interface/inactive");
    private static final ResourceLocation LED_INACTIVE_MODEL = createIdentifier("block/disk/led_inactive");

    private final ResourceLocation baseModel;

    DiskInterfaceUnbakedGeometry(final DyeColor color) {
        this.baseModel = createIdentifier("block/disk_interface/base_" + color.getName());
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        modelGetter.apply(baseModel).resolveParents(modelGetter);
        modelGetter.apply(INACTIVE_MODEL).resolveParents(modelGetter);
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
                           final ItemOverrides overrides) {
        return new DiskInterfaceBakedModel(
            requireNonNull(baker.bake(baseModel, modelState, spriteGetter)),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, INACTIVE_MODEL),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, baseModel),
            new DiskModelBaker(modelState, baker, spriteGetter),
            new RotationTranslationModelBaker(modelState, baker, spriteGetter, LED_INACTIVE_MODEL)
        );
    }
}
