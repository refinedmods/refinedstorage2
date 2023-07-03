package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ImporterScreen extends AbstractBaseScreen<ImporterContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    public ImporterScreen(final ImporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new FilterModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FILTER_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FUZZY_MODE)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
