package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SideButtonWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SynchronizationSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSynchronizer, List<Component>> tooltips = new HashMap<>();

    public SynchronizationSideButtonWidget(final GridContainerMenu<?> menu, final TooltipRenderer tooltipRenderer, final List<GridSynchronizer> synchronizers) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        synchronizers.forEach(synchronizer -> tooltips.put(synchronizer, calculateTooltip(synchronizer)));
    }

    private static OnPress createPressAction(final GridContainerMenu<?> menu) {
        return btn -> menu.toggleSynchronizer();
    }

    private List<Component> calculateTooltip(final GridSynchronizer synchronizer) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.synchronizer"));
        lines.add(synchronizer.getTitle().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected ResourceLocation getTextureIdentifier() {
        return menu.getSynchronizer().getTextureIdentifier();
    }

    @Override
    protected int getXTexture() {
        return menu.getSynchronizer().getXTexture();
    }

    @Override
    protected int getYTexture() {
        return menu.getSynchronizer().getYTexture();
    }

    @Override
    public void onTooltip(final Button buttonWidget, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(menu.getSynchronizer()), mouseX, mouseY);
    }
}
