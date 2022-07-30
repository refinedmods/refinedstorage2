package com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.transform;

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
        final SpriteFinder finder = SpriteFinder.get(
            Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
        );
        if (finder == null) {
            return true;
        }
        final TextureAtlasSprite sprite = finder.find(quad, 0);
        if (sprite == null) {
            return true;
        }
        if (!emissiveSprite.equals(sprite.getName())) {
            return true;
        }
        doTransform(quad);
        return true;
    }

    private void doTransform(final MutableQuadView quad) {
        quad.lightmap(0, LightTexture.pack(15, 15));
        quad.lightmap(1, LightTexture.pack(15, 15));
        quad.lightmap(2, LightTexture.pack(15, 15));
        quad.lightmap(3, LightTexture.pack(15, 15));
    }
}
