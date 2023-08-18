package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ResourceAmountScreen extends AbstractAmountScreen<ResourceAmountScreen.DummyContainerMenu, Double> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/resource_amount.png");
    private static final MutableComponent TITLE = createTranslation("gui", "amount");

    private final ResourceSlot slot;

    public ResourceAmountScreen(final Screen parent, final Inventory playerInventory, final ResourceSlot slot) {
        super(
            new DummyContainerMenu(slot),
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
                .withMinAmount(1D)
                .withMaxAmount(slot.getMaxAmountWhenModifying())
                .withResetAmount(1D)
                .build(),
            DoubleAmountOperations.INSTANCE
        );
        this.slot = slot;
        this.imageWidth = 172;
        this.imageHeight = 99;
    }

    @Override
    protected void accept(final Double amount) {
        slot.changeAmountOnClient(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    public static class DummyContainerMenu extends AbstractResourceContainerMenu {
        protected DummyContainerMenu(final ResourceSlot slot) {
            super(null, 0);
            addSlot(slot.forAmountScreen(89, 48));
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot) {
            return ItemStack.EMPTY;
        }
    }
}
