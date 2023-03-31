package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.DetectorModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DetectorScreen extends AbstractBaseScreen<DetectorContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/detector.png");

    @Nullable
    private EditBox amountBox;

    public DetectorScreen(final DetectorContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 43;
        this.imageWidth = 176;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();
        if (amountBox == null) {
            amountBox = new EditBox(
                font,
                leftPos + 41,
                topPos + 24,
                50,
                font.lineHeight,
                Component.literal("")
            );
        } else {
            amountBox.setX(leftPos + 41);
            amountBox.setY(topPos + 24);
        }
        amountBox.setFocus(false);
        amountBox.setCanLoseFocus(true);
        amountBox.setBordered(false);
        amountBox.setFilter(value -> value.matches("\\d*"));
        amountBox.setValue(String.valueOf(menu.getAmount()));
        amountBox.setResponder(value -> {
            try {
                final long amount = value.trim().isEmpty() ? 0 : Long.parseLong(value);
                menu.changeAmountOnClient(amount);
            } catch (final NumberFormatException e) {
                // do nothing
            }
        });
        addWidget(amountBox);
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new DetectorModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.DETECTOR_MODE),
            this::renderComponentTooltip
        ));
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (amountBox != null) {
            amountBox.render(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (amountBox != null && amountBox.charTyped(unknown1, unknown2)) || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        return (amountBox != null && amountBox.keyPressed(key, scanCode, modifiers))
            || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
