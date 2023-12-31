package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class CraftingGridScreen extends AbstractGridScreen<CraftingGridContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/crafting_grid.png");
    private static final int CLEAR_BUTTON_SIZE = 7;

    @Nullable
    private ImageButton clearToNetworkButton;

    private boolean filteringBasedOnCraftingMatrixItems;

    public CraftingGridScreen(final CraftingGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 156);
        this.inventoryLabelY = 134;
        this.imageWidth = 193;
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

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        if (filteringBasedOnCraftingMatrixItems) {
            renderCraftingMatrixFilteringHighlights(graphics);
        }
    }

    private void renderCraftingMatrixFilteringHighlights(final GuiGraphics graphics) {
        for (final Slot slot : getMenu().getCraftingMatrixSlots()) {
            if (!slot.hasItem()) {
                continue;
            }
            renderCraftingMatrixFilteringHighlight(graphics, slot);
        }
    }

    private void renderCraftingMatrixFilteringHighlight(final GuiGraphics graphics, final Slot slot) {
        graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 224, 238, 18, 18);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        final boolean mayFilterOnCraftingMatrixItems = hoveredSlot != null
            && hoveredSlot.container instanceof ResultContainer
            && hasShiftDown()
            && hasControlDown();
        if (mayFilterOnCraftingMatrixItems && !filteringBasedOnCraftingMatrixItems) {
            filteringBasedOnCraftingMatrixItems = true;
            getMenu().filterBasedOnCraftingMatrixItems();
            if (searchField != null) {
                searchField.setEditable(false);
            }
        } else if (!mayFilterOnCraftingMatrixItems && filteringBasedOnCraftingMatrixItems) {
            getMenu().stopFilteringBasedOnCraftingMatrixItems();
            filteringBasedOnCraftingMatrixItems = false;
            if (searchField != null) {
                searchField.setEditable(true);
            }
        }
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
    public void onClose() {
        switch (Platform.INSTANCE.getConfig().getCraftingGrid().getCraftingMatrixCloseBehavior()) {
            case CLEAR_TO_NETWORK -> getMenu().clear(false);
            case CLEAR_TO_INVENTORY -> getMenu().clear(true);
        }
        super.onClose();
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
