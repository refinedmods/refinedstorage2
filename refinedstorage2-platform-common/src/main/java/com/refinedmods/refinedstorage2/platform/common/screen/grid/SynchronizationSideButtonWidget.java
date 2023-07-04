package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SynchronizationSideButtonWidget extends AbstractSideButtonWidget {
    private final AbstractGridContainerMenu menu;
    private final Map<GridSynchronizer, List<Component>> tooltips = new HashMap<>();

    public SynchronizationSideButtonWidget(final AbstractGridContainerMenu menu,
                                           final List<GridSynchronizer> synchronizers) {
        super(createPressAction(menu));
        this.menu = menu;
        synchronizers.forEach(synchronizer -> tooltips.put(synchronizer, calculateTooltip(synchronizer)));
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
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
    protected List<Component> getSideButtonTooltip() {
        return tooltips.get(menu.getSynchronizer());
    }
}
