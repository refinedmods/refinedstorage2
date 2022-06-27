package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ProgressWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/widgets.png");

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final DoubleSupplier progressSupplier;
    private final TooltipRenderer tooltipRenderer;
    private final Supplier<List<Component>> tooltipSupplier;

    public ProgressWidget(final int x, final int y, final int width, final int height, final DoubleSupplier progressSupplier, final TooltipRenderer tooltipRenderer, final Supplier<List<Component>> tooltipSupplier) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.progressSupplier = progressSupplier;
        this.tooltipRenderer = tooltipRenderer;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float delta) {
        final int correctedHeight = (int) (progressSupplier.getAsDouble() * height);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        RenderSystem.enableDepthTest();
        final int zOffset = getBlitOffset();
        setBlitOffset(200);
        blit(poseStack, x, y + height - correctedHeight, 179, height - correctedHeight, width, correctedHeight);
        setBlitOffset(zOffset);
        RenderSystem.disableDepthTest();

        if (isHovered(mouseX, mouseY)) {
            tooltipRenderer.render(poseStack, tooltipSupplier.get(), mouseX, mouseY);
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        // intentionally empty
    }
}
