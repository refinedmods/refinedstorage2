package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import java.awt.Color;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class MissingItemRecipeTransferError implements IRecipeTransferError {
    private static final Color COLOR = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    private static final List<FormattedCharSequence> MISSING_MESSAGE = List.of(
        Component.translatable("jei.tooltip.transfer").getVisualOrderText(),
        Component.translatable("jei.tooltip.error.recipe.transfer.missing").withStyle(ChatFormatting.RED)
            .getVisualOrderText()
    );

    private final List<IRecipeSlotView> slotsWithMissingItems;

    public MissingItemRecipeTransferError(final List<IRecipeSlotView> slotsWithMissingItems) {
        this.slotsWithMissingItems = slotsWithMissingItems;
    }

    @Override
    public Type getType() {
        return Type.COSMETIC;
    }

    @Override
    public int getButtonHighlightColor() {
        return COLOR.getRGB();
    }

    @Override
    public void showError(final GuiGraphics graphics,
                          final int mouseX,
                          final int mouseY,
                          final IRecipeSlotsView recipeSlotsView,
                          final int recipeX,
                          final int recipeY) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(recipeX, recipeY, 0);
        slotsWithMissingItems.forEach(slot -> slot.drawHighlight(graphics, COLOR.getRGB()));
        poseStack.popPose();
        final Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            graphics.renderTooltip(Minecraft.getInstance().font, MISSING_MESSAGE, mouseX, mouseY);
        }
    }
}
