package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FilterModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<FilterMode> property;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> blockModeTooltip;
    private final List<Component> allowModeTooltip;

    public FilterModeSideButtonWidget(final ClientProperty<FilterMode> property,
                                      final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
        this.blockModeTooltip = calculateTooltip(FilterMode.BLOCK);
        this.allowModeTooltip = calculateTooltip(FilterMode.ALLOW);
    }

    private static OnPress createPressAction(final ClientProperty<FilterMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static FilterMode toggle(final FilterMode filterMode) {
        return filterMode == FilterMode.ALLOW ? FilterMode.BLOCK : FilterMode.ALLOW;
    }

    private List<Component> calculateTooltip(final FilterMode filterMode) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "filter_mode"));
        lines.add(createTranslation(
            "gui",
            "filter_mode." + filterMode.toString().toLowerCase(Locale.ROOT)
        ).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return property.getValue() == FilterMode.BLOCK ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    public void onTooltip(final Button button, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(
            poseStack,
            property.getValue() == FilterMode.BLOCK ? blockModeTooltip : allowModeTooltip,
            mouseX,
            mouseY
        );
    }
}
