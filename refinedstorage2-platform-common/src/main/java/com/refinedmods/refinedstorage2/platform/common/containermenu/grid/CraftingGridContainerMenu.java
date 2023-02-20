package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftingGridContainerMenu extends AbstractGridContainerMenu {
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT = 69;

    private final Player player;
    private final CraftingGridSource source;

    public CraftingGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, buf);
        this.source = new ClientCraftingGridSource();
        this.player = playerInventory.player;
        addSlots(0);
    }

    public CraftingGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final CraftingGridBlockEntity grid) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, grid);
        this.source = new CraftingGridSourceImpl(grid);
        this.player = playerInventory.player;
        addSlots(0);
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        return !(slot instanceof CraftingGridResultSlot);
    }

    @Override
    public ItemStack quickMoveStack(final Player actor, final int slotIndex) {
        final Slot slot = getSlot(slotIndex);
        if (slot instanceof CraftingGridResultSlot resultSlot) {
            return resultSlot.onQuickCraft(actor);
        }
        return super.quickMoveStack(actor, slotIndex);
    }

    @Override
    public void addSlots(final int playerInventoryY) {
        super.addSlots(playerInventoryY);
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                final int slotX = 26 + ((x % 3) * 18);
                final int slotY = playerInventoryY
                    - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT
                    + ((y % 3) * 18);
                addSlot(new Slot(source.getCraftingMatrix(), x + y * 3, slotX, slotY));
            }
        }
        addSlot(new CraftingGridResultSlot(
            player,
            source,
            130 + 4,
            playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT + 18
        ));
    }
}
