package com.refinedmods.refinedstorage2.platform.fabric.support.render.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class EmissiveTransform implements RenderContext.QuadTransform {
    private final ResourceLocation emissiveSprite;

    public EmissiveTransform(final ResourceLocation emissiveSprite) {
        this.emissiveSprite = emissiveSprite;
    }

    @Override
    public boolean transform(final MutableQuadView quad) {
        doTransform(quad);
        return true;
    }

    private void doTransform(final MutableQuadView quad) {
        final SpriteFinder finder = SpriteFinder.get(getAtlas());
        if (finder == null) {
            return;
        }
        final TextureAtlasSprite sprite = finder.find(quad);
        if (sprite == null) {
            return;
        }
        if (!emissiveSprite.equals(sprite.contents().name())) {
            return;
        }
        applyLightmap(quad);
    }

    @SuppressWarnings("deprecation")
    private static TextureAtlas getAtlas() {
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
    }

    private void applyLightmap(final MutableQuadView quad) {
        quad.lightmap(0, LightTexture.pack(15, 15));
        quad.lightmap(1, LightTexture.pack(15, 15));
        quad.lightmap(2, LightTexture.pack(15, 15));
        quad.lightmap(3, LightTexture.pack(15, 15));
    }
}
