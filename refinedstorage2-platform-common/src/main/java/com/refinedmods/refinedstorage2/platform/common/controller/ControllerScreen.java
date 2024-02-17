package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.RedstoneModeSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerScreen extends AbstractBaseScreen<ControllerContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/controller.png");

    @Nullable
    private ProgressWidget progressWidget;

    public ControllerScreen(final ControllerContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 94;
        this.imageWidth = 176;
        this.imageHeight = 189;
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
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
