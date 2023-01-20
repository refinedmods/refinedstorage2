package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FuzzyModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<Boolean> property;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> tooltipWhenOn;
    private final List<Component> tooltipWhenOff;

    public FuzzyModeSideButtonWidget(final ClientProperty<Boolean> property, final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
        this.tooltipWhenOn = calculateTooltip(true);
        this.tooltipWhenOff = calculateTooltip(false);
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    private List<Component> calculateTooltip(final boolean fuzzyMode) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "fuzzy_mode"));
        lines.add(createTranslation("gui", "fuzzy_mode." + (fuzzyMode ? "on" : "off")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 192;
    }

    @Override
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(
            poseStack,
            Boolean.TRUE.equals(property.getValue()) ? tooltipWhenOn : tooltipWhenOff,
            mouseX,
            mouseY
        );
    }
}
