package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.KeyMappings;
import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.CustomButton;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class CraftingGridScreen extends AbstractGridScreen<AbstractCraftingGridContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/crafting_grid.png");

    private static final WidgetSprites CLEAR_BUTTON_TO_PLAYER_INVENTORY_SPRITES = new WidgetSprites(
        createIdentifier("widget/move_down"),
        createIdentifier("widget/move_down_disabled"),
        createIdentifier("widget/move_down_focused"),
        createIdentifier("widget/move_down_disabled")
    );
    private static final WidgetSprites CLEAR_BUTTON_TO_NETWORK_SPRITES = new WidgetSprites(
        createIdentifier("widget/move_up"),
        createIdentifier("widget/move_up_disabled"),
        createIdentifier("widget/move_up_focused"),
        createIdentifier("widget/move_up_disabled")
    );
    private static final ResourceLocation CRAFTING_MATRIX_FILTERING_SLOT_HIGHLIGHT = createIdentifier(
        "crafting_grid/crafting_matrix_filtering_slot_highlight"
    );

    @Nullable
    private CustomButton clearToNetworkButton;

    private boolean filteringBasedOnCraftingMatrixItems;

    public CraftingGridScreen(final AbstractCraftingGridContainerMenu menu,
                              final Inventory inventory,
                              final Component title) {
        super(menu, inventory, title, 156);
        this.inventoryLabelY = 134;
        this.imageWidth = 193;
        this.imageHeight = 229;
    }

    @Override
    protected void init() {
        super.init();

        final int clearToNetworkButtonX = getClearButtonX(0);
        final int clearToInventoryButtonX = getClearButtonX(1);
        final int clearButtonY = topPos + imageHeight - bottomHeight + 4;

        clearToNetworkButton = createClearButton(clearToNetworkButtonX, clearButtonY, false);
        setClearToNetworkButtonActive(getMenu().isActive());
        getMenu().setActivenessListener(this::setClearToNetworkButtonActive);
        addRenderableWidget(clearToNetworkButton);
        addRenderableWidget(createClearButton(clearToInventoryButtonX, clearButtonY, true));
    }

    private int getClearButtonX(final int i) {
        return leftPos + 81 + ((CLEAR_BUTTON_SIZE + 2) * i);
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
            graphics.blitSprite(
                CRAFTING_MATRIX_FILTERING_SLOT_HIGHLIGHT,
                leftPos + slot.x - 1,
                topPos + slot.y - 1,
                18,
                18
            );
        }
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

    private CustomButton createClearButton(final int x, final int y, final boolean toPlayerInventory) {
        final MutableComponent text = createTranslation(
            "gui",
            "crafting_grid.move." + (toPlayerInventory ? "inventory" : "network")
        );
        final KeyMapping keyMapping = getClearButtonKeyMapping(toPlayerInventory);
        if (keyMapping != null) {
            text.append("\n").append(keyMapping.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY));
        }
        final WidgetSprites widgetSprites = toPlayerInventory
            ? CLEAR_BUTTON_TO_PLAYER_INVENTORY_SPRITES
            : CLEAR_BUTTON_TO_NETWORK_SPRITES;
        final CustomButton button = new CustomButton(
            x,
            y,
            CLEAR_BUTTON_SIZE,
            CLEAR_BUTTON_SIZE,
            widgetSprites,
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
        final CraftingGridMatrixCloseBehavior behavior = Platform.INSTANCE.getConfig()
            .getCraftingGrid()
            .getCraftingMatrixCloseBehavior();
        if (behavior == CraftingGridMatrixCloseBehavior.CLEAR_TO_NETWORK) {
            getMenu().clear(false);
        } else if (behavior == CraftingGridMatrixCloseBehavior.CLEAR_TO_INVENTORY) {
            getMenu().clear(true);
        }
        super.onClose();
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        final boolean hoveredSlotValidForHelp = hoveredSlot != null
            && hoveredSlot.container instanceof ResultContainer
            && hoveredSlot.hasItem();
        if (getMenu().getCarried().isEmpty() && hoveredSlotValidForHelp && !filteringBasedOnCraftingMatrixItems) {
            final ItemStack stack = hoveredSlot.getItem();
            final List<Component> lines = getTooltipFromContainerItem(stack);
            final List<ClientTooltipComponent> processedLines = Platform.INSTANCE.processTooltipComponents(
                stack,
                graphics,
                x,
                stack.getTooltipImage(),
                lines
            );
            processedLines.add(HelpClientTooltipComponent.create(createTranslation(
                "gui",
                "crafting_grid.press_shift_ctrl_to_only_show_items_used_in_crafting"
            )));
            Platform.INSTANCE.renderTooltip(graphics, processedLines, x, y);
            return;
        }
        super.renderTooltip(graphics, x, y);
    }
}
