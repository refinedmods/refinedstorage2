package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ResourceAmountScreen
    extends AbstractAmountScreen<ResourceAmountScreen.SingleResourceContainerMenu, Double> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/resource_amount.png");
    private static final MutableComponent TITLE = createTranslation("gui", "configure_amount");

    private final ResourceSlot slot;

    public ResourceAmountScreen(final Screen parent, final Inventory playerInventory, final ResourceSlot slot) {
        super(
            new SingleResourceContainerMenu(slot, 89, 48),
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(slot.getDisplayAmount())
                .withIncrementsTop(1, 10, 64)
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(7, 72, 0))
                .withAmountFieldPosition(new Vector3f(9, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(114, 22, 0))
                .withMinAmount(() -> slot.getResource() != null
                    ? slot.getResource().getResourceType().getDisplayAmount(1)
                    : 1)
                .withMaxAmount(slot.getMaxAmountWhenModifying())
                .withResetAmount(1D)
                .build(),
            ExpressionAmountOperations.INSTANCE
        );
        this.slot = slot;
        this.imageWidth = 180;
        this.imageHeight = 99;
    }

    @Override
    protected boolean confirm(final Double value) {
        slot.changeAmountOnClient(value);
        return true;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    public static class SingleResourceContainerMenu extends AbstractResourceContainerMenu {
        public SingleResourceContainerMenu(final ResourceSlot slot, final int x, final int y) {
            super(null, 0);
            addSlot(slot.forAmountScreen(x, y));
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
