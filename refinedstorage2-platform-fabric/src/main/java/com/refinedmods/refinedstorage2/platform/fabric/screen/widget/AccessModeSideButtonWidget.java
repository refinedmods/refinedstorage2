package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.AccessModeAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AccessModeSideButtonWidget extends SideButtonWidget {
    private final AccessModeAccessor accessModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final Map<AccessMode, List<Text>> tooltips = new EnumMap<>(AccessMode.class);

    public AccessModeSideButtonWidget(AccessModeAccessor accessModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(accessModeAccessor));
        this.accessModeAccessor = accessModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(AccessMode.values()).forEach(accessMode -> tooltips.put(accessMode, calculateTooltip(accessMode)));
    }

    private static PressAction createPressAction(AccessModeAccessor accessModeAccessor) {
        return btn -> accessModeAccessor.setAccessMode(accessModeAccessor.getAccessMode().toggle());
    }

    private List<Text> calculateTooltip(AccessMode accessMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "access_mode"));
        lines.add(Rs2Mod.createTranslation("gui", "access_mode." + accessMode.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        switch (accessModeAccessor.getAccessMode()) {
            case INSERT_EXTRACT:
                return 0;
            case INSERT:
                return 16;
            case EXTRACT:
                return 32;
            default:
                return 0;
        }
    }

    @Override
    protected int getYTexture() {
        return 240;
    }

    @Override
    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        tooltipRenderer.render(matrices, tooltips.get(accessModeAccessor.getAccessMode()), mouseX, mouseY);
    }
}
