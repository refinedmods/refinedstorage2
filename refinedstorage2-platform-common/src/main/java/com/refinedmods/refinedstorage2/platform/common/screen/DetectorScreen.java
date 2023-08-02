package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.DoubleAmountOperations;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.DetectorModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class DetectorScreen extends AbstractAmountScreen<DetectorContainerMenu, Double> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/detector.png");

    public DetectorScreen(final DetectorContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(
            menu,
            null,
            playerInventory,
            text,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(menu.getAmount())
                .withIncrementsTop(1, 10, 64)
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsTopStartPosition(new Vector3f(40, 20, 0))
                .withIncrementsBottomStartPosition(new Vector3f(40, 70, 0))
                .withAmountFieldWidth(59)
                .withAmountFieldPosition(new Vector3f(45, 51, 0))
                .withActionButtonsEnabled(false)
                .withMinAmount(0D)
                .withResetAmount(0D)
                .build(),
            DoubleAmountOperations.INSTANCE
        );
        this.inventoryLabelY = 94;
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.DETECTOR
        ));
        addSideButton(new DetectorModeSideButtonWidget(getMenu().getProperty(PropertyTypes.DETECTOR_MODE)));
    }

    @Override
    protected void accept(final Double amount) {
        getMenu().changeAmountOnClient(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }
}
