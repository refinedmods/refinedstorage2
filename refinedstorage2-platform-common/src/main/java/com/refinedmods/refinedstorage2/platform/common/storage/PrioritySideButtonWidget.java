package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.common.support.amount.PriorityScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class PrioritySideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "priority");
    private static final Component HELP = createTranslation("gui", "priority.storage_help");

    private final ClientProperty<Integer> property;

    public PrioritySideButtonWidget(final ClientProperty<Integer> property,
                                    final Inventory playerInventory,
                                    final Screen parent) {
        super(createPressAction(property, playerInventory, parent));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<Integer> property,
                                             final Inventory playerInventory,
                                             final Screen parent) {
        return btn -> Minecraft.getInstance().setScreen(new PriorityScreen(property, parent, playerInventory));
    }

    @Override
    protected int getXTexture() {
        return 0;
    }

    @Override
    protected int getYTexture() {
        return 208;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return Component.literal(String.valueOf(property.getValue()));
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
