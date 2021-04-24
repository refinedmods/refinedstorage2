package com.refinedmods.refinedstorage2.fabric.screen.widget;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ProgressWidget extends DrawableHelper implements Drawable, Element {
    private static final Identifier SPRITE = Rs2Mod.createIdentifier("textures/gui/widgets.png");

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final DoubleSupplier progressSupplier;
    private final TooltipRenderer tooltipRenderer;
    private final Supplier<List<Text>> tooltipSupplier;

    public ProgressWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier, TooltipRenderer tooltipRenderer, Supplier<List<Text>> tooltipSupplier) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.progressSupplier = progressSupplier;
        this.tooltipRenderer = tooltipRenderer;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int correctedHeight = (int) (progressSupplier.getAsDouble() * (double) height);

        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().bindTexture(SPRITE);
        drawTexture(matrices, x, y + height - correctedHeight, 179, height - correctedHeight, width, correctedHeight);

        boolean hovered = mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
        if (hovered) {
            tooltipRenderer.render(matrices, tooltipSupplier.get(), mouseX, mouseY);
        }
    }
}
