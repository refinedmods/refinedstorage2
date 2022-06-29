package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.FilterModeAccessor;
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
    private final FilterModeAccessor filterModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> blockModeTooltip;
    private final List<Component> allowModeTooltip;

    public FilterModeSideButtonWidget(final FilterModeAccessor filterModeAccessor,
                                      final TooltipRenderer tooltipRenderer) {
        super(createPressAction(filterModeAccessor));
        this.filterModeAccessor = filterModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.blockModeTooltip = calculateTooltip(FilterMode.BLOCK);
        this.allowModeTooltip = calculateTooltip(FilterMode.ALLOW);
    }

    private static OnPress createPressAction(final FilterModeAccessor filterModeAccessor) {
        return btn -> filterModeAccessor.setFilterMode(filterModeAccessor.getFilterMode().toggle());
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
        return filterModeAccessor.getFilterMode() == FilterMode.BLOCK ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    public void onTooltip(final Button button, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(
                poseStack,
                filterModeAccessor.getFilterMode() == FilterMode.BLOCK ? blockModeTooltip : allowModeTooltip,
                mouseX,
                mouseY
        );
    }
}
