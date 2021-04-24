package com.refinedmods.refinedstorage2.fabric.screen.widget;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.PriorityScreen;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screenhandler.PriorityAccessor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PrioritySideButtonWidget extends SideButtonWidget {
    private final PriorityAccessor priorityAccessor;
    private final TooltipRenderer tooltipRenderer;

    public PrioritySideButtonWidget(PriorityAccessor priorityAccessor, Screen parent, TooltipRenderer tooltipRenderer) {
        super(createPressAction(priorityAccessor, parent));
        this.priorityAccessor = priorityAccessor;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static PressAction createPressAction(PriorityAccessor priorityAccessor, Screen parent) {
        return btn -> MinecraftClient.getInstance().openScreen(new PriorityScreen(priorityAccessor, parent));
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
    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "priority"));
        lines.add(new LiteralText(String.valueOf(priorityAccessor.getPriority())).formatted(Formatting.GRAY));
        tooltipRenderer.render(matrices, lines, mouseX, mouseY);
    }
}
