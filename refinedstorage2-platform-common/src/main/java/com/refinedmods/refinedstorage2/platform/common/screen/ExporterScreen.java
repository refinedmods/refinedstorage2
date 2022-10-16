package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ExporterSchedulingModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ExporterScreen extends AbstractBaseScreen<ExporterContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/exporter.png");

    public ExporterScreen(final ExporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();

        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new ExporterSchedulingModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.EXPORTER_SCHEDULING_MODE),
            this::renderComponentTooltip
        ));
    }

    @Override
    protected int getResourceFilterButtonX() {
        return super.getResourceFilterButtonX() - 34;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
