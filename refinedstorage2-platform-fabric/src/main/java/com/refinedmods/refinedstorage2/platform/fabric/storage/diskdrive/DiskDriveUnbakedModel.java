package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadRotators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class DiskDriveUnbakedModel implements UnbakedModel {
    private static final ResourceLocation BASE_MODEL = createIdentifier("block/disk_drive/base");
    private static final ResourceLocation LED_INACTIVE_MODEL = createIdentifier("block/disk/led_inactive");

    private final QuadRotators quadRotators;

    public DiskDriveUnbakedModel(final QuadRotators quadRotators) {
        this.quadRotators = quadRotators;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        final Set<ResourceLocation> dependencies = new HashSet<>();
        dependencies.add(BASE_MODEL);
        dependencies.add(LED_INACTIVE_MODEL);
        dependencies.addAll(PlatformApi.INSTANCE.getStorageContainerItemHelper().getDiskModels());
        return dependencies;
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter) {
        modelGetter.apply(BASE_MODEL).resolveParents(modelGetter);
        PlatformApi.INSTANCE.getStorageContainerItemHelper().getDiskModels().forEach(
            diskModel -> modelGetter.apply(diskModel).resolveParents(modelGetter)
        );
        modelGetter.apply(LED_INACTIVE_MODEL).resolveParents(modelGetter);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState state,
                           final ResourceLocation location) {
        final Map<Item, BakedModel> diskModels = PlatformApi.INSTANCE.getStorageContainerItemHelper()
            .getDiskModelsByItem()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> requireNonNull(baker.bake(entry.getValue(), state))
            ));
        return new DiskDriveBakedModel(
            requireNonNull(baker.bake(BASE_MODEL, state)),
            diskModels,
            requireNonNull(baker.bake(LED_INACTIVE_MODEL, state)),
            quadRotators
        );
    }
}
