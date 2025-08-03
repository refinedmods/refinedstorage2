package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.AutoSelectedSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AutocrafterManagerScreen extends AbstractStretchingScreen<AutocrafterManagerContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/autocrafter_manager.png");
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final ResourceLocation AUTOCRAFTER_NAME = createIdentifier("autocrafter_manager/autocrafter_name");
    private static final int COLUMNS = 9;
    private static final int INACTIVE_COLOR = 0xFF5B5B5B;

    private static final MutableComponent HELP_ALL =
        createTranslation("gui", "autocrafter_manager.search_mode.all.help");
    private static final MutableComponent HELP_PATTERN_INPUTS =
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_inputs.help");
    private static final MutableComponent HELP_PATTERN_OUTPUTS =
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_outputs.help");
    private static final MutableComponent HELP_AUTOCRAFTER_NAMES =
        createTranslation("gui", "autocrafter_manager.search_mode.autocrafter_names.help");

    @Nullable
    private SearchFieldWidget searchField;

    public AutocrafterManagerScreen(final AutocrafterManagerContainerMenu menu,
                                    final Inventory playerInventory,
                                    final Component title) {
        super(menu, playerInventory, new TextMarquee(title, 70));
        this.inventoryLabelY = 75;
        this.imageWidth = 193;
        this.imageHeight = 176;
    }

    @Override
    protected void init(final int rows) {
        super.init(rows);

        getMenu().setListener(() -> {
            resize();
            updateScrollbar();
            scrollbarChanged(rows);
        });

        if (searchField == null) {
            searchField = new SearchFieldWidget(
                font,
                leftPos + 94 + 1,
                topPos + 6 + 1,
                73 - 6,
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            searchField.setX(leftPos + 94 + 1);
            searchField.setY(topPos + 6 + 1);
        }
        updateScrollbar();

        addWidget(searchField);
        searchField.setResponder(value -> getMenu().setQuery(value));

        addRenderableWidget(new SearchIconWidget(
            leftPos + 79,
            topPos + 5,
            () -> getSearchModeHelp().copy().withStyle(ChatFormatting.GRAY),
            searchField
        ));

        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new ViewTypeSideButtonWidget(getMenu()));
        addSideButton(new SearchModeSideButtonWidget(getMenu(), this::getSearchModeHelp));
        addSideButton(new AutoSelectedSideButtonWidget(searchField));
    }

    private Component getSearchModeHelp() {
        return switch (menu.getSearchMode()) {
            case ALL -> HELP_ALL;
            case PATTERN_INPUTS -> HELP_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> HELP_PATTERN_OUTPUTS;
            case AUTOCRAFTER_NAMES -> HELP_AUTOCRAFTER_NAMES;
        };
    }

    private void updateScrollbar() {
        final int totalRows = menu.getGroups()
            .stream()
            .map(group -> group.isVisible() ? group.getVisibleRows() + 1 : 0)
            .reduce(0, Integer::sum);
        updateScrollbar(totalRows);
    }

    @Override
    protected void scrollbarChanged(final int rows) {
        super.scrollbarChanged(rows);
        final int scrollbarOffset = getScrollbarOffset();
        for (int i = 0; i < menu.getAutocrafterSlots().size(); ++i) {
            final AutocrafterManagerSlot slot = menu.getAutocrafterSlots().get(i);
            Platform.INSTANCE.setSlotY(slot, slot.getOriginalY() - scrollbarOffset);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (searchField != null) {
            searchField.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (searchField != null && searchField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (searchField != null && searchField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected void renderRows(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        if (!menu.isActive()) {
            graphics.fill(
                RenderType.guiOverlay(),
                x + 7 + 1,
                y + TOP_HEIGHT + 1,
                x + 7 + (ROW_SIZE * COLUMNS) - 1,
                y + TOP_HEIGHT + 1 + (ROW_SIZE * rows) - 2,
                INACTIVE_COLOR
            );
            return;
        }
        renderGroups(graphics, x, y, topHeight, rows);
        renderSlotContents(graphics, mouseX, mouseY, y, topHeight, rows);
    }

    private void renderGroups(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows) {
        final int rowX = x + 7;
        int rowY = y + topHeight - getScrollbarOffset();
        for (final AutocrafterManagerContainerMenu.ViewGroup group : menu.getGroups()) {
            if (!group.isVisible()) {
                continue;
            }
            if (!isOutOfFrame(y, topHeight, rows, rowY)) {
                graphics.blitSprite(AUTOCRAFTER_NAME, rowX, rowY, 162, ROW_SIZE);
                graphics.drawString(font, group.getName(), rowX + 4, rowY + 6, 4210752, false);
            }
            renderGroup(graphics, y, topHeight, rows, group, rowX, rowY);
            rowY += (group.getVisibleRows() + 1) * ROW_SIZE;
        }
    }

    private static void renderGroup(final GuiGraphics graphics,
                                    final int y,
                                    final int topHeight,
                                    final int rows,
                                    final AutocrafterManagerContainerMenu.ViewGroup group,
                                    final int rowX,
                                    final int rowY) {
        int j = 0;
        for (final AutocrafterManagerContainerMenu.SubViewGroup subGroup : group.getSubViewGroups()) {
            for (int i = 0; i < subGroup.getVisibleSlots(); i++) {
                final int slotX = rowX + ((j % COLUMNS) * 18);
                final int slotY = rowY + 18 + ((j / COLUMNS) * 18);
                if (!isOutOfFrame(y, topHeight, rows, slotY)) {
                    graphics.blitSprite(Sprites.SLOT, slotX, slotY, 18, 18);
                }
                ++j;
            }
        }
    }

    private void renderSlotContents(final GuiGraphics graphics,
                                    final int mouseX,
                                    final int mouseY,
                                    final int y,
                                    final int topHeight,
                                    final int rows) {
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        for (final Slot slot : menu.getAutocrafterSlots()) {
            if (isOutOfFrame(y, topHeight, rows, topPos + slot.y)) {
                continue;
            }
            super.renderSlot(graphics, slot);
            final boolean hovering = mouseX >= slot.x + leftPos
                && mouseX < slot.x + leftPos + 16
                && mouseY >= slot.y + topPos
                && mouseY < slot.y + topPos + 16;
            if (slot.isActive() && hovering) {
                renderSlotHighlight(graphics, slot.x, slot.y, 0);
            }
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderSlot(final GuiGraphics guiGraphics, final Slot slot) {
        if (slot instanceof AutocrafterManagerSlot) {
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }

    private static boolean isOutOfFrame(final int y,
                                        final int topHeight,
                                        final int rows,
                                        final int rowY) {
        return (rowY < y + topHeight - ROW_SIZE)
            || (rowY > y + topHeight + (ROW_SIZE * rows));
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphics graphics, final int x, final int y, final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE);
        }
    }

    @Override
    protected int getBottomHeight() {
        return 99;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
