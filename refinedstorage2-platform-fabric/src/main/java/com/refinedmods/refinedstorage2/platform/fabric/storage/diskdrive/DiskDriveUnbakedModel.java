package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class DiskDriveUnbakedModel implements UnbakedModel {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive/base");
    private static final ResourceLocation DISK_MODEL = createIdentifier("block/disk/disk");
    private static final ResourceLocation LED_INACTIVE_MODEL = createIdentifier("block/disk/led_inactive");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Set.of(BASE_MODEL, DISK_MODEL, LED_INACTIVE_MODEL);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter) {
        modelGetter.apply(BASE_MODEL).resolveParents(modelGetter);
        modelGetter.apply(DISK_MODEL).resolveParents(modelGetter);
        modelGetter.apply(LED_INACTIVE_MODEL).resolveParents(modelGetter);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState state,
                           final ResourceLocation location) {
        return new DiskDriveBakedModel(
            requireNonNull(baker.bake(BASE_MODEL, state)),
            requireNonNull(baker.bake(DISK_MODEL, state)),
            requireNonNull(baker.bake(LED_INACTIVE_MODEL, state))
        );
    }
}
