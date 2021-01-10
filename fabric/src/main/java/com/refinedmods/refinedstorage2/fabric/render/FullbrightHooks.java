package com.refinedmods.refinedstorage2.fabric.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.mixin.BakedQuadAccessor;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;

/**
 * @link https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/fabric/master/src/main/java/appeng/hooks/UnlitQuadHooks.java
 */
public class FullbrightHooks {
    private static final VertexFormat VERTEX_FORMAT = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    private static final int LIGHT_OFFSET = getLightOffset();
    private static final int FULLBRIGHT_LIGHT_UV = LightmapTextureManager.pack(15, 15);

    private static final ThreadLocal<Boolean> ENABLE_FULLBRIGHT_EXTENSIONS = new ThreadLocal<>();

    public static void beginDeserializingModel(Identifier location) {
        String namespace = location.getNamespace();
        if (namespace.equals(RefinedStorage2Mod.ID)) {
            ENABLE_FULLBRIGHT_EXTENSIONS.set(true);
        }
    }

    public static void endDeserializingModel() {
        ENABLE_FULLBRIGHT_EXTENSIONS.set(false);
    }

    public static boolean isFullbrightExtensionEnabled() {
        Boolean b = ENABLE_FULLBRIGHT_EXTENSIONS.get();
        return b != null && b;
    }

    public static ModelElementFace enhanceModelElementFace(ModelElementFace modelElement, JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (JsonHelper.getBoolean(jsonObject, "fullbright", false)) {
            return new FullbrightModelElementFace(modelElement.cullFace, modelElement.tintIndex, modelElement.textureId, modelElement.textureData);
        }
        return modelElement;
    }

    public static BakedQuad makeFullbright(BakedQuad quad) {
        int[] vertexData = quad.getVertexData().clone();
        int stride = VERTEX_FORMAT.getVertexSizeInteger();

        for (int i = 0; i < 4; i++) {
            vertexData[stride * i + LIGHT_OFFSET] = FULLBRIGHT_LIGHT_UV;
        }
        Sprite sprite = ((BakedQuadAccessor) quad).getSprite();

        return new BakedQuad(vertexData, quad.getColorIndex(), quad.getFace(), sprite, false);
    }

    public static class FullbrightModelElementFace extends ModelElementFace {
        public FullbrightModelElementFace(Direction cullFace, int tintIndex, String texture, ModelElementTexture blockFaceUV) {
            super(cullFace, tintIndex, texture, blockFaceUV);
        }
    }

    private static int getLightOffset() {
        int offset = 0;
        for (VertexFormatElement element : VERTEX_FORMAT.getElements()) {
            if (element == VertexFormats.LIGHT_ELEMENT) {
                if (element.getFormat() != VertexFormatElement.Format.SHORT) {
                    throw new UnsupportedOperationException("Expected light map format to be of type SHORT");
                }
                if (offset % 4 != 0) {
                    throw new UnsupportedOperationException("Expected light map offset to be 4-byte aligned");
                }
                return offset / 4;
            }
            offset += element.getSize();
        }
        throw new UnsupportedOperationException("Failed to find the lightmap index in the block vertex format");
    }
}
