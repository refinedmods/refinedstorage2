package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO check parity with RS 1.
public class ResourceAmountScreen extends AbstractAmountScreen {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/resource_amount.png");
    private static final MutableComponent TITLE = createTranslation("gui", "amount");

    private static final int[] INCREMENTS_TOP = {1, 10, 64};
    private static final int[] INCREMENTS_BOTTOM = {1, -10, -64};

    private static final Vector3f AMOUNT_FIELD_POSITION = new Vector3f(9, 51, 0);
    private static final Vector3f ACTION_BUTTON_POSITION = new Vector3f(114, 22, 0);

    private final ResourceFilterSlot slot;

    public ResourceAmountScreen(final Screen parent,
                                final Inventory playerInventory,
                                final ResourceFilterSlot slot) {
        super(new DummyContainerMenu(slot), parent, playerInventory, TITLE);
        this.slot = slot;
        this.imageWidth = 172;
        this.imageHeight = 99;
    }

    @Override
    protected int getInitialAmount() {
        final FilteredResource filteredResource = slot.getFilteredResource();
        if (filteredResource == null) {
            return 0;
        }
        return (int) filteredResource.getAmount();
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
        return 1;
    }

    @Override
    protected int getMaxAmount() {
        final FilteredResource filteredResource = slot.getFilteredResource();
        if (filteredResource == null) {
            return 0;
        }
        return (int) slot.getFilteredResource().getMaxAmount();
    }

    @Override
    protected int getResetAmount() {
        return 1;
    }

    @Override
    protected void accept(final int amount) {
        slot.changeAmountOnClient(amount);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected boolean tryOpenResourceFilterAmountScreen(final Slot clickedSlot, final ClickType type) {
        return false;
    }

    @Override
    protected void renderResourceFilterSlotAmount(final PoseStack poseStack,
                                                  final int x,
                                                  final int y,
                                                  final FilteredResource filteredResource) {
        // should not render amount here
    }

    private static class DummyContainerMenu extends AbstractContainerMenu {
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
