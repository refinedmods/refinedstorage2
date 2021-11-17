package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RedstoneModeSideButtonWidget extends SideButtonWidget {
    private final Map<RedstoneMode, List<Component>> tooltips = new EnumMap<>(RedstoneMode.class);
    private final TooltipRenderer tooltipRenderer;
    private final RedstoneModeAccessor redstoneModeAccessor;

    public RedstoneModeSideButtonWidget(RedstoneModeAccessor redstoneModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(redstoneModeAccessor));
        this.tooltipRenderer = tooltipRenderer;
        this.redstoneModeAccessor = redstoneModeAccessor;
        Arrays.stream(RedstoneMode.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(RedstoneModeAccessor redstoneModeAccessor) {
        return btn -> redstoneModeAccessor.setRedstoneMode(redstoneModeAccessor.getRedstoneMode().toggle());
    }

    private List<Component> calculateTooltip(RedstoneMode type) {
        List<Component> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "redstone_mode"));
        lines.add(Rs2Mod.createTranslation("gui", "redstone_mode." + type.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        switch (redstoneModeAccessor.getRedstoneMode()) {
            case IGNORE:
                return 0;
            case HIGH:
                return 16;
            case LOW:
                return 32;
            default:
                return 0;
        }
    }

    @Override
    protected int getYTexture() {
        return 0;
    }

    @Override
    public void onTooltip(Button button, PoseStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(redstoneModeAccessor.getRedstoneMode()), mouseX, mouseY);
    }
}
