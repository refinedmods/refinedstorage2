package com.refinedmods.refinedstorage2.platform.common.importer;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ImporterScreen extends AbstractFilterScreen<ImporterContainerMenu> {
    public ImporterScreen(final ImporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            createTranslation("gui", "importer.filter_mode.allow.help"),
            createTranslation("gui", "importer.filter_mode.block.help")
        ));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.EXTRACTING_SOURCE
        ));
    }
}
