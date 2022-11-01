package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import com.mojang.math.Vector3f;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO check parity with RS 1.
public class PriorityScreen extends AbstractAmountScreen {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/priority.png");
    private static final MutableComponent PRIORITY_TEXT = createTranslation("gui", "priority");

    private final ClientProperty<Integer> property;

    public PriorityScreen(final ClientProperty<Integer> property,
                          final Screen parent,
                          final Inventory playerInventory) {
        super(
            parent,
            playerInventory,
            PRIORITY_TEXT,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.create()
                .withInitialAmount(property.get())
                .withIncrementsTop(1, 5, 10)
                .withIncrementsBottom(-1, -5, -10)
                .withAmountFieldPosition(new Vector3f(19, 48, 0))
                .withActionButtonsStartPosition(new Vector3f(107, 18, 0))
                .withMinAmount(Integer.MIN_VALUE)
                .withMaxAmount(Integer.MAX_VALUE)
                .withResetAmount(0)
                .build()
        );
        this.property = property;
        this.imageWidth = 164;
        this.imageHeight = 92;
    }

    @Override
    protected void accept(final int amount) {
        property.setValue(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
