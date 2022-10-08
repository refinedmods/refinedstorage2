package com.refinedmods.refinedstorage2.platform.common.screen;

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

    private static final int[] INCREMENTS_TOP = {1, 5, 10};
    private static final int[] INCREMENTS_BOTTOM = {-1, -5, -10};

    private static final Vector3f AMOUNT_FIELD_POSITION = new Vector3f(19, 48, 0);
    private static final Vector3f ACTION_BUTTON_POSITION = new Vector3f(107, 18, 0);

    private final ClientProperty<Integer> property;

    public PriorityScreen(final ClientProperty<Integer> property,
                          final Screen parent,
                          final Inventory playerInventory) {
        super(parent, playerInventory, PRIORITY_TEXT);
        this.property = property;
        this.imageWidth = 164;
        this.imageHeight = 92;
    }

    @Override
    protected int getInitialAmount() {
        return property.get();
    }

    @Override
    protected int[] getIncrementsTop() {
        return INCREMENTS_TOP;
    }

    @Override
    protected int[] getIncrementsBottom() {
        return INCREMENTS_BOTTOM;
    }

    @Override
    protected Vector3f getAmountFieldPosition() {
        return AMOUNT_FIELD_POSITION;
    }

    @Override
    protected Vector3f getActionButtonPosition() {
        return ACTION_BUTTON_POSITION;
    }

    @Override
    protected int getMinAmount() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected int getMaxAmount() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getResetAmount() {
        return 0;
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
