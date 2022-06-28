package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ExactModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExactModeSideButtonWidget extends SideButtonWidget {
    private final ExactModeAccessor exactModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> tooltipWhenOn;
    private final List<Component> tooltipWhenOff;

    public ExactModeSideButtonWidget(final ExactModeAccessor exactModeAccessor, final TooltipRenderer tooltipRenderer) {
        super(createPressAction(exactModeAccessor));
        this.exactModeAccessor = exactModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.tooltipWhenOn = calculateTooltip(true);
        this.tooltipWhenOff = calculateTooltip(false);
    }

    private static OnPress createPressAction(final ExactModeAccessor exactModeAccessor) {
        return btn -> exactModeAccessor.setExactMode(!exactModeAccessor.isExactMode());
    }

    private List<Component> calculateTooltip(final boolean exactMode) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "exact_mode"));
        lines.add(createTranslation("gui", "exact_mode." + (exactMode ? "on" : "off")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return exactModeAccessor.isExactMode() ? 0 : 16;
    }

    @Override
    protected int getYTexture() {
        return 192;
    }

    @Override
    public void onTooltip(final Button button, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(
                poseStack,
                exactModeAccessor.isExactMode() ? tooltipWhenOn : tooltipWhenOff,
                mouseX,
                mouseY
        );
    }
}
