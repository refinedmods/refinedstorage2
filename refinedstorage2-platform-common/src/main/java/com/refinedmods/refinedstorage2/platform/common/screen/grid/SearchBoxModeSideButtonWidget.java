package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.search.PlatformSearchBoxModeImpl;
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

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<PlatformSearchBoxModeImpl, List<Component>> tooltips = new HashMap<>();

    public SearchBoxModeSideButtonWidget(GridContainerMenu screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static OnPress createPressAction(GridContainerMenu screenHandler) {
        return btn -> screenHandler.setSearchBoxMode((PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.next(screenHandler.getSearchBoxMode()));
    }

    private List<Component> calculateTooltip(PlatformSearchBoxModeImpl searchBoxMode) {
        List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.search_box_mode"));
        lines.add(searchBoxMode.getName().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected ResourceLocation getTextureIdentifier() {
        return screenHandler.getSearchBoxMode().getTextureIdentifier();
    }

    @Override
    protected int getXTexture() {
        return screenHandler.getSearchBoxMode().getTextureX();
    }

    @Override
    protected int getYTexture() {
        return screenHandler.getSearchBoxMode().getTextureY();
    }

    @Override
    public void onTooltip(Button buttonWidget, PoseStack poseStack, int mouseX, int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.computeIfAbsent(screenHandler.getSearchBoxMode(), this::calculateTooltip), mouseX, mouseY);
    }
}
