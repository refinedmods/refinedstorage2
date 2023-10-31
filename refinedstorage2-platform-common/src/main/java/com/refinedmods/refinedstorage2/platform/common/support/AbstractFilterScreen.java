package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractFilterScreen<T extends AbstractBaseContainerMenu> extends AbstractBaseScreen<T> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    protected AbstractFilterScreen(final T menu,
                                   final Inventory playerInventory,
                                   final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
