package com.refinedmods.refinedstorage2.fabric.screen.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.handler.property.TwoWaySyncProperty;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class RedstoneModeSideButtonWidget extends SideButtonWidget {
    private final Map<RedstoneMode, List<Text>> tooltips = new EnumMap<>(RedstoneMode.class);
    private final TooltipRenderer tooltipRenderer;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    public RedstoneModeSideButtonWidget(TwoWaySyncProperty<RedstoneMode> redstoneModeProperty, TooltipRenderer tooltipRenderer) {
        super(createPressAction(redstoneModeProperty));
        this.tooltipRenderer = tooltipRenderer;
        this.redstoneModeProperty = redstoneModeProperty;
        Arrays.stream(RedstoneMode.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private List<Text> calculateTooltip(RedstoneMode type) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.redstone_mode"));
        lines.add(new TranslatableText("gui.refinedstorage2.redstone_mode." + type.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    private static PressAction createPressAction(TwoWaySyncProperty<RedstoneMode> redstoneModeProperty) {
        return btn -> redstoneModeProperty.syncToServer(redstoneModeProperty.getDeserialized().toggle());
    }

    @Override
    protected int getXTexture() {
        switch (redstoneModeProperty.getDeserialized()) {
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
    public void onTooltip(ButtonWidget button, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(redstoneModeProperty.getDeserialized()), mouseX, mouseY);
    }
}
