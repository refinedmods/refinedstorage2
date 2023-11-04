package com.refinedmods.refinedstorage2.platform.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadRotators;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

public class PortableGridUnbakedModel implements UnbakedModel {
    private static final ResourceLocation ACTIVE_MODEL = createIdentifier("block/portable_grid/active");
    private static final ResourceLocation INACTIVE_MODEL = createIdentifier("block/portable_grid/inactive");

    private final QuadRotators quadRotators;

    public PortableGridUnbakedModel(final QuadRotators quadRotators) {
        this.quadRotators = quadRotators;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Set.of(ACTIVE_MODEL, INACTIVE_MODEL);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter) {
        modelGetter.apply(ACTIVE_MODEL).resolveParents(modelGetter);
        modelGetter.apply(INACTIVE_MODEL).resolveParents(modelGetter);
        PlatformApi.INSTANCE.getStorageContainerItemHelper().getDiskModels().forEach(
            diskModel -> modelGetter.apply(diskModel).resolveParents(modelGetter)
        );
    }

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
        return new PortableGridBakedModel(
            requireNonNull(baker.bake(ACTIVE_MODEL, state)),
            requireNonNull(baker.bake(INACTIVE_MODEL, state)),
            diskModels,
            quadRotators
        );
    }
}
