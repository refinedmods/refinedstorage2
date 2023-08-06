package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ResourceAmountScreen extends AbstractAmountScreen<ResourceAmountScreen.DummyContainerMenu, Long> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/resource_amount.png");
    private static final MutableComponent TITLE = createTranslation("gui", "amount");

    private final ResourceFilterSlot slot;

    public ResourceAmountScreen(final Screen parent,
                                final Inventory playerInventory,
                                final ResourceFilterSlot slot) {
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
                .withMaxAmount(getMaxAmount(slot))
                .withResetAmount(1L)
                .build(),
            LongAmountOperations.INSTANCE
        );
        this.slot = slot;
        this.imageWidth = 172;
        this.imageHeight = 99;
    }

    private static long getInitialAmount(final ResourceFilterSlot slot) {
        return slot.getFilteredResource() == null ? 0 : slot.getFilteredResource().getAmount();
    }

    private static long getMaxAmount(final ResourceFilterSlot slot) {
        return slot.getFilteredResource() == null ? 0 : slot.getFilteredResource().getMaxAmount();
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
    protected boolean tryOpenResourceFilterAmountScreen(final ResourceFilterSlot clickedSlot) {
        return false;
    }

    @Override
    protected void renderResourceFilterSlotAmount(final GuiGraphics graphics,
                                                  final int x,
                                                  final int y,
                                                  final FilteredResource<?> filteredResource) {
        // should not render amount here
    }

    public static class DummyContainerMenu extends AbstractContainerMenu {
        protected DummyContainerMenu(final ResourceFilterSlot slot) {
            super(null, 0);
            addSlot(slot.atPosition(89, 48));
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
