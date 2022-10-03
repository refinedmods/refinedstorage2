package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerScreen extends AbstractBaseScreen<ControllerContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/controller.png");

    private final ProgressWidget progressWidget;

    public ControllerScreen(final ControllerContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);

        this.inventoryLabelY = 94;
        this.imageWidth = 176;
        this.imageHeight = 189;

        this.progressWidget = new ProgressWidget(
            80,
            20,
            16,
            70,
            this::getPercentageFull,
            this::renderComponentTooltip,
            this::createTooltip
        );
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            this::renderComponentTooltip
        ));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    private double getPercentageFull() {
        return (double) getMenu().getStored() / (double) getMenu().getCapacity();
    }

    private List<Component> createTooltip() {
        return Collections.singletonList(createTranslation(
            "misc",
            "stored_with_capacity",
            QuantityFormatter.format(getMenu().getStored()),
            QuantityFormatter.format(getMenu().getCapacity())
        ));
    }

    @Override
    protected void renderLabels(final PoseStack poseStack, final int mouseX, final int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        progressWidget.render(poseStack, mouseX - leftPos, mouseY - topPos, 0);
    }
}
