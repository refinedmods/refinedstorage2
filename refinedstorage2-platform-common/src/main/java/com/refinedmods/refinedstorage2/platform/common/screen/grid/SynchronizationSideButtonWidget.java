package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.GridSynchronizationType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SideButtonWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SynchronizationSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSynchronizationType, List<Component>> tooltips = new EnumMap<>(GridSynchronizationType.class);

    public SynchronizationSideButtonWidget(GridContainerMenu<?> menu, TooltipRenderer tooltipRenderer, GridSynchronizer synchronizer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSynchronizationType.values()).forEach(type -> tooltips.put(type, calculateTooltip(type, synchronizer)));
    }

    private static OnPress createPressAction(GridContainerMenu<?> menu) {
        return btn -> menu.setSynchronizationType(menu.getSynchronizationType().toggle());
    }

    private List<Component> calculateTooltip(GridSynchronizationType type, GridSynchronizer synchronizer) {
        List<Component> lines = new ArrayList<>();
        lines.add(synchronizer.getTitle());
        lines.add(createTranslatedValue(type).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private MutableComponent createTranslatedValue(GridSynchronizationType type) {
        return switch (type) {
            case OFF -> createTranslation("gui", "grid.synchronization.off");
            case ON -> createTranslation("gui", "grid.synchronization.on");
            case TWO_WAY -> createTranslation("gui", "grid.synchronization.two_way");
        };
    }

    @Override
    protected int getXTexture() {
        return switch (menu.getSynchronizationType()) {
            case OFF -> 64;
            case ON -> 48;
            case TWO_WAY -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 97;
    }

    @Override
    public void onTooltip(Button buttonWidget, PoseStack poseStack, int mouseX, int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(menu.getSynchronizationType()), mouseX, mouseY);
    }
}
