package com.refinedmods.refinedstorage2.platform.forge.support.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class RenderTypes {
    public static final RenderType DISK_LED = RenderType.create(
        "disk_led",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        32565,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
            .createCompositeState(false)
    );

    private RenderTypes() {
    }
}
