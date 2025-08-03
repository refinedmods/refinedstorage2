package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.support.containermenu.AbstractSingleAmountContainerMenu;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractSingleAmountScreen<T extends AbstractSingleAmountContainerMenu>
    extends AbstractAmountScreen<T, Double> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/amount_with_inventory.png");

    protected AbstractSingleAmountScreen(final T containerMenu,
                                         final Inventory playerInventory,
                                         final Component title,
                                         final Double initialAmount,
                                         final Supplier<Double> minAmount) {
        super(containerMenu, null, playerInventory, title,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(initialAmount)
                .withIncrementsTop(1, 10, 64)
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsTopStartPosition(new Vector3f(40, 20, 0))
                .withIncrementsBottomStartPosition(new Vector3f(40, 70, 0))
                .withAmountFieldWidth(59)
                .withAmountFieldPosition(new Vector3f(45, 51, 0))
                .withActionButtonsEnabled(false)
                .withMinAmount(minAmount)
                .withResetAmount(minAmount.get())
                .build(),
            ExpressionAmountOperations.INSTANCE);
        this.inventoryLabelY = 94;
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    @Override
    protected boolean confirm(final Double value) {
        getMenu().changeAmountOnClient(value);
        return true;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        renderPlayerInventoryTitle(graphics);
    }
}
