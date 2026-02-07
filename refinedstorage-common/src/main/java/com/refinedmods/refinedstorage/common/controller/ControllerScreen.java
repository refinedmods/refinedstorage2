package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.ProgressWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ControllerScreen extends AbstractBaseScreen<ControllerContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/controller.png");

    @Nullable
    private ProgressWidget progressWidget;

    public ControllerScreen(final ControllerContainerMenu menu,
                            final Inventory playerInventory,
                            final Component title) {
        super(menu, playerInventory, title, 176, 189);
        this.inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            createTranslation("gui", "controller.redstone_mode_help")
        ));
        if (progressWidget == null) {
            progressWidget = new ProgressWidget(
                leftPos + 80,
                topPos + 20,
                16,
                70,
                getMenu().getEnergyInfo()::getPercentageFull,
                getMenu().getEnergyInfo()::createTooltip
            );
        } else {
            progressWidget.setX(leftPos + 80);
            progressWidget.setY(topPos + 20);
        }
        addRenderableWidget(progressWidget);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
