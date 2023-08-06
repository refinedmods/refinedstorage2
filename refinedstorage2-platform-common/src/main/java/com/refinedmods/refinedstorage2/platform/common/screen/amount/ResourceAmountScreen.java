package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ResourceAmountScreen extends AbstractAmountScreen<ResourceAmountScreen.DummyContainerMenu, Long> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/resource_amount.png");
    private static final MutableComponent TITLE = createTranslation("gui", "amount");

    private final ResourceSlot slot;

    public ResourceAmountScreen(final Screen parent,
                                final Inventory playerInventory,
                                final ResourceSlot slot) {
        super(
            new DummyContainerMenu(slot),
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Long>create()
                .withInitialAmount(getInitialAmount(slot))
                .withIncrementsTop(1, 10, 64)
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(7, 72, 0))
                .withAmountFieldPosition(new Vector3f(9, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(114, 22, 0))
                .withMinAmount(1L)
                .withMaxAmount(slot.getMaxAmount())
                .withResetAmount(1L)
                .build(),
            LongAmountOperations.INSTANCE
        );
        this.slot = slot;
        this.imageWidth = 172;
        this.imageHeight = 99;
    }

    private static long getInitialAmount(final ResourceSlot slot) {
        return slot.getContents() == null ? 0 : slot.getContents().getAmount();
    }

    @Override
    protected void accept(final Long amount) {
        slot.changeAmountOnClient(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected boolean tryOpenResourceAmountScreen(final ResourceSlot clickedSlot) {
        return false;
    }

    @Override
    protected <R> void renderResourceSlotAmount(final GuiGraphics graphics,
                                                final int x,
                                                final int y,
                                                final long amount,
                                                final ResourceRendering<R> rendering) {
        // should not render amount here
    }

    public static class DummyContainerMenu extends AbstractResourceContainerMenu {
        protected DummyContainerMenu(final ResourceSlot slot) {
            super(null, 0);
            addSlot(slot.atPosition(89, 48));
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot) {
            return ItemStack.EMPTY;
        }
    }
}
