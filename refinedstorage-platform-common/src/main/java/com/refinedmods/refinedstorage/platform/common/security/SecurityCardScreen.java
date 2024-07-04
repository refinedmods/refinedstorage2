package com.refinedmods.refinedstorage.platform.common.security;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SecurityCardScreen extends AbstractSecurityCardScreen<SecurityCardContainerMenu> {
    private static final int BOUND_PLAYER_BUTTON_RIGHT_PADDING = 6;
    private static final int BOUND_PLAYER_BUTTON_WIDTH = 80;

    public SecurityCardScreen(final SecurityCardContainerMenu menu,
                              final Inventory playerInventory,
                              final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init(final int rows) {
        super.init(rows);
        final Component boundToText = Component.literal(menu.getBoundTo().name());
        final Button boundPlayerButton = Button.builder(boundToText, this::toggleBoundPlayer)
            .pos(leftPos + imageWidth - BOUND_PLAYER_BUTTON_RIGHT_PADDING - BOUND_PLAYER_BUTTON_WIDTH, topPos + 4)
            .size(BOUND_PLAYER_BUTTON_WIDTH, 14)
            .build();
        addRenderableWidget(boundPlayerButton);
    }

    private void toggleBoundPlayer(final Button button) {
        if (menu.getPlayers().isEmpty()) {
            return;
        }
        final PlayerBoundSecurityCardData.Player currentPlayer = menu.getBoundTo();
        final int index = menu.getPlayers().indexOf(currentPlayer);
        final int nextIndex = (index + 1) % menu.getPlayers().size();
        final PlayerBoundSecurityCardData.Player nextPlayer = menu.getPlayers().get(nextIndex);
        menu.changeBoundPlayer(nextPlayer);
        button.setMessage(Component.literal(nextPlayer.name()));
    }
}
