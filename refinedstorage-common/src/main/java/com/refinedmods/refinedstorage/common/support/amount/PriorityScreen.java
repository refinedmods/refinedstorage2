package com.refinedmods.refinedstorage.common.support.amount;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class PriorityScreen extends AbstractAmountScreen<AbstractAmountScreen.DefaultDummyContainerMenu, Integer> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/priority.png");

    private final IntConsumer priorityChanged;

    public PriorityScreen(final MutableComponent title,
                          final int priority,
                          final IntConsumer priorityChanged,
                          final Screen parent,
                          final Inventory playerInventory) {
        super(
            new DefaultDummyContainerMenu(),
            parent,
            playerInventory,
            title,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Integer>create()
                .withInitialAmount(priority)
                .withIncrementsTop(1, 5, 10)
                .withIncrementsBottom(-1, -5, -10)
                .withAmountFieldPosition(new Vector3f(19, 48, 0))
                .withActionButtonsStartPosition(new Vector3f(107, 18, 0))
                .withMinAmount(() -> Integer.MIN_VALUE)
                .withMaxAmount(Integer.MAX_VALUE)
                .withResetAmount(0)
                .build(),
            IntegerAmountOperations.INSTANCE,
            172, 92
        );
        this.priorityChanged = priorityChanged;
    }

    @Override
    protected boolean confirm(final Integer amount) {
        priorityChanged.accept(amount);
        return true;
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
