package com.refinedmods.refinedstorage.neoforge.support.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class RenderTypes {
    private static final RenderPipeline DISK_LEDS_PIPELINE = RenderPipeline
        .builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .withLocation(createIdentifier("pipeline/disk_leds"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .build();

    public static final RenderType DISK_LEDS = RenderType.create(
        MOD_ID + "_disk_leds",
        RenderSetup.builder(DISK_LEDS_PIPELINE).bufferSize(32565).createRenderSetup()
    );

    private RenderTypes() {
    }
}
