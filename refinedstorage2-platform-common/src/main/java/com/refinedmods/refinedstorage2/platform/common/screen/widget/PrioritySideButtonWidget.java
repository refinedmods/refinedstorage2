package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.PriorityScreen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class PrioritySideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<Integer> property;
    private final TooltipRenderer tooltipRenderer;

    public PrioritySideButtonWidget(final ClientProperty<Integer> property,
                                    final Inventory playerInventory,
                                    final Screen parent,
                                    final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property, playerInventory, parent));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static OnPress createPressAction(final ClientProperty<Integer> property,
                                             final Inventory playerInventory,
                                             final Screen parent) {
        return btn -> Minecraft.getInstance().setScreen(new PriorityScreen(property, parent, playerInventory));
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
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "priority"));
        lines.add(Component.literal(String.valueOf(property.getValue())).withStyle(ChatFormatting.GRAY));
        tooltipRenderer.render(poseStack, lines, mouseX, mouseY);
    }
}
