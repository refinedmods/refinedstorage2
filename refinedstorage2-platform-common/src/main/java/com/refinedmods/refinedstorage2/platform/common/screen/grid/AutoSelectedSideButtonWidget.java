package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class AutoSelectedSideButtonWidget extends AbstractSideButtonWidget {
    private final AbstractGridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> yes;
    private final List<Component> no;

    public AutoSelectedSideButtonWidget(final AbstractGridContainerMenu<?> menu,
                                        final TooltipRenderer tooltipRenderer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        this.yes = calculateTooltip(true);
        this.no = calculateTooltip(false);
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu<?> menu) {
        return btn -> menu.setAutoSelected(!menu.isAutoSelected());
    }

    private List<Component> calculateTooltip(final boolean autoSelected) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.auto_selected"));
        lines.add(Component.translatable("gui." + (autoSelected ? "yes" : "no")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return menu.isAutoSelected() ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 96;
    }

    @Override
    public void onTooltip(final Button buttonWidget, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, menu.isAutoSelected() ? yes : no, mouseX, mouseY);
    }
}
