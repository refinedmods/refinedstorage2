package com.refinedmods.refinedstorage2.platform.common.support.amount;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class PriorityScreen extends AbstractAmountScreen<AbstractAmountScreen.DefaultDummyContainerMenu, Integer> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/priority.png");
    private static final MutableComponent PRIORITY_TEXT = createTranslation("gui", "priority");

    private final ClientProperty<Integer> property;

    public PriorityScreen(final ClientProperty<Integer> property,
                          final Screen parent,
                          final Inventory playerInventory) {
        super(
            new DefaultDummyContainerMenu(),
            parent,
            playerInventory,
            PRIORITY_TEXT,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Integer>create()
                .withInitialAmount(property.get())
                .withIncrementsTop(1, 5, 10)
                .withIncrementsBottom(-1, -5, -10)
                .withAmountFieldPosition(new Vector3f(19, 48, 0))
                .withActionButtonsStartPosition(new Vector3f(107, 18, 0))
                .withMinAmount(Integer.MIN_VALUE)
                .withMaxAmount(Integer.MAX_VALUE)
                .withResetAmount(0)
                .build(),
            IntegerAmountOperations.INSTANCE
        );
        this.property = property;
        this.imageWidth = 164;
        this.imageHeight = 92;
    }

    @Override
    protected void accept(final Integer amount) {
        property.setValue(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
