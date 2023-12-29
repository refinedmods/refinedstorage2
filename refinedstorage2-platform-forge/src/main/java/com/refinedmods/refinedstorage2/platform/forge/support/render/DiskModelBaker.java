package com.refinedmods.refinedstorage2.platform.forge.support.render;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.world.item.Item;

public class DiskModelBaker {
    private final Map<Item, RotationTranslationModelBaker> bakers;

    public DiskModelBaker(final ModelState state,
                          final ModelBaker baker,
                          final Function<Material, TextureAtlasSprite> spriteGetter) {
        this.bakers = PlatformApi.INSTANCE.getStorageContainerItemHelper()
            .getDiskModelsByItem()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new RotationTranslationModelBaker(state, baker, spriteGetter, entry.getValue())
            ));
    }

    @Nullable
    public RotationTranslationModelBaker forDisk(final Item item) {
        return bakers.get(item);
    }
}
