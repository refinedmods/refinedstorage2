package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.PriorityAccessor;
import com.refinedmods.refinedstorage2.platform.common.screen.PriorityScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class PrioritySideButtonWidget extends SideButtonWidget {
    private final PriorityAccessor priorityAccessor;
    private final TooltipRenderer tooltipRenderer;

    public PrioritySideButtonWidget(PriorityAccessor priorityAccessor, Inventory playerInventory, Screen parent, TooltipRenderer tooltipRenderer) {
        super(createPressAction(priorityAccessor, playerInventory, parent));
        this.priorityAccessor = priorityAccessor;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static OnPress createPressAction(PriorityAccessor priorityAccessor, Inventory playerInventory, Screen parent) {
        return btn -> Minecraft.getInstance().setScreen(new PriorityScreen(priorityAccessor, parent, playerInventory));
    }

    @Override
    protected int getXTexture() {
        return 0;
    }

    @Override
    protected int getYTexture() {
        return 208;
    }

    @Override
    public void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "priority"));
        lines.add(Component.literal(String.valueOf(priorityAccessor.getPriority())).withStyle(ChatFormatting.GRAY));
        tooltipRenderer.render(poseStack, lines, mouseX, mouseY);
    }
}
