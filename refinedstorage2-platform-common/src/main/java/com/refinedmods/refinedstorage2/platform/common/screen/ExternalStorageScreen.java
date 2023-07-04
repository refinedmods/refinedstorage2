package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ExternalStorageScreen extends AbstractBaseScreen<ExternalStorageContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    private final Inventory playerInventory;

    public ExternalStorageScreen(final ExternalStorageContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = 42;
        this.imageWidth = 176;
        this.imageHeight = 137;
        this.playerInventory = inventory;
    }

    @Override
    protected void init() {
        super.init();

        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new FilterModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FILTER_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FUZZY_MODE)));
        addSideButton(new AccessModeSideButtonWidget(getMenu().getProperty(PropertyTypes.ACCESS_MODE)));
        addSideButton(new PrioritySideButtonWidget(
            getMenu().getProperty(PropertyTypes.PRIORITY),
            playerInventory,
            this
        ));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
