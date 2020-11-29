package com.refinedmods.refinedstorage2.fabric.screen.handler.grid;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.fabric.screen.handler.BaseScreenHandler;
import net.minecraft.entity.player.PlayerInventory;

public class GridScreenHandler extends BaseScreenHandler implements GridEventHandler {
    private final PlayerInventory playerInventory;

    public GridScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;

        addSlots(0);
    }

    public void addSlots(int playerInventoryY) {
        slots.clear();

        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    @Override
    public void onInsertFromCursor(boolean single) {
        System.out.println("Inserting " + playerInventory.getCursorStack() + " (single=" + single + ")");
    }
}
