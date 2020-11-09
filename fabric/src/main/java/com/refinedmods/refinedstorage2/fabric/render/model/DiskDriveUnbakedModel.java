package com.refinedmods.refinedstorage2.fabric.render.model;

import com.google.common.collect.ImmutableSet;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.render.model.baked.DiskDriveBakedModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public class DiskDriveUnbakedModel implements BasicUnbakedModel {
    private static final Identifier BASE_MODEL = new Identifier(RefinedStorage2Mod.ID, "block/disk_drive_base");
    private static final Identifier DISK_MODEL = new Identifier(RefinedStorage2Mod.ID, "block/disk");

    @Override
    public Collection<Identifier> getModelDependencies() {
        return new ImmutableSet.Builder<Identifier>()
            .add(BASE_MODEL)
            .add(DISK_MODEL)
            .build();
    }

    @Override
    public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return new DiskDriveBakedModel(
            loader.bake(BASE_MODEL, rotationContainer),
            loader.bake(DISK_MODEL, rotationContainer)
        );
    }
}
