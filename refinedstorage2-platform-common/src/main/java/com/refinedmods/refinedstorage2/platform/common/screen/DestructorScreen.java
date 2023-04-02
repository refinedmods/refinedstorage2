package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.DestructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.DestructorPickupItemsSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DestructorScreen extends AbstractBaseScreen<DestructorContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    public DestructorScreen(final DestructorContainerMenu menu, final Inventory playerInventory, final Component text) {
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
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new DestructorPickupItemsSideButtonWidget(
            getMenu().getProperty(PropertyTypes.DESTRUCTOR_PICKUP_ITEMS),
            this::renderComponentTooltip
        ));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
