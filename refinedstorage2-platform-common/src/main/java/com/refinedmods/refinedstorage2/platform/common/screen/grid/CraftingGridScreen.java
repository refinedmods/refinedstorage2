package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class CraftingGridScreen extends AbstractGridScreen<CraftingGridContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/crafting_grid.png");
    private static final int CLEAR_BUTTON_SIZE = 7;

    @Nullable
    private ImageButton clearToNetworkButton;

    public CraftingGridScreen(final CraftingGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 156);
        this.inventoryLabelY = 134;
        this.imageWidth = 227;
        this.imageHeight = 229;
    }

    @Override
    protected void init() {
        super.init();

        final int clearToNetworkButtonX = leftPos + 82;
        final int clearToInventoryButtonX = clearToNetworkButtonX + CLEAR_BUTTON_SIZE + 3;
        final int clearButtonY = topPos + imageHeight - bottomHeight + 4;

        clearToNetworkButton = createClearButton(clearToNetworkButtonX, clearButtonY, 242, false);
        setClearToNetworkButtonActive(getMenu().isActive());
        getMenu().setActivenessListener(this::setClearToNetworkButtonActive);
        addRenderableWidget(clearToNetworkButton);
        addRenderableWidget(createClearButton(clearToInventoryButtonX, clearButtonY, 249, true));
    }

    private void setClearToNetworkButtonActive(final boolean active) {
        if (clearToNetworkButton == null) {
            return;
        }
        clearToNetworkButton.active = active;
    }

    private ImageButton createClearButton(final int x,
                                          final int y,
                                          final int textureX,
                                          final boolean toPlayerInventory) {
        final MutableComponent text = Component.translatable(
            "gui.refinedstorage2.crafting_grid.move." + (toPlayerInventory ? "inventory" : "network")
        );
        final KeyMapping keyMapping = getClearButtonKeyMapping(toPlayerInventory);
        if (keyMapping != null) {
            text.append("\n").append(keyMapping.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY));
        }
        final ImageButton button = new ImageButton(
            x,
            y,
            CLEAR_BUTTON_SIZE,
            CLEAR_BUTTON_SIZE,
            textureX,
            235,
            CLEAR_BUTTON_SIZE,
            TEXTURE,
            256,
            256,
            b -> getMenu().clear(toPlayerInventory),
            text
        );
        button.setTooltip(Tooltip.create(text));
        return button;
    }

    @Nullable
    private KeyMapping getClearButtonKeyMapping(final boolean toPlayerInventory) {
        return toPlayerInventory
            ? wrapUnbound(KeyMappings.INSTANCE.getClearCraftingGridMatrixToInventory())
            : wrapUnbound(KeyMappings.INSTANCE.getClearCraftingGridMatrixToNetwork());
    }

    @Nullable
    private KeyMapping wrapUnbound(@Nullable final KeyMapping keyMapping) {
        return keyMapping == null || keyMapping.isUnbound() ? null : keyMapping;
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (KeyMappings.INSTANCE.getClearCraftingGridMatrixToInventory() != null
            && Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getClearCraftingGridMatrixToInventory())) {
            getMenu().clear(true);
        } else if (KeyMappings.INSTANCE.getClearCraftingGridMatrixToNetwork() != null
            && Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getClearCraftingGridMatrixToNetwork())) {
            getMenu().clear(false);
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
