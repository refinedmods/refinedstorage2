package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.AutoSelectedSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class AutocrafterManagerScreen extends AbstractStretchingScreen<AutocrafterManagerContainerMenu>
    implements PatternOutputRenderingScreen {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/autocrafter_manager.png");
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final Identifier AUTOCRAFTER_NAME = createIdentifier("autocrafter_manager/autocrafter_name");
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
        super(menu, playerInventory, new TextMarquee(title, 70), 193, 176);
        this.inventoryLabelY = 75;
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
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (searchField != null) {
            searchField.extractRenderState(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (searchField != null && searchField.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (searchField != null && searchField.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void renderRows(final GuiGraphicsExtractor graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        if (!menu.isActive()) {
            graphics.fill(
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

    private void renderGroups(final GuiGraphicsExtractor graphics,
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
                graphics.blitSprite(GUI_TEXTURED, AUTOCRAFTER_NAME, rowX, rowY, 162, ROW_SIZE);
                graphics.text(font, group.getName(), rowX + 4, rowY + 6, -12566464, false);
            }
            renderGroup(graphics, y, topHeight, rows, group, rowX, rowY);
            rowY += (group.getVisibleRows() + 1) * ROW_SIZE;
        }
    }

    private static void renderGroup(final GuiGraphicsExtractor graphics,
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
                    graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, slotX, slotY, 18, 18);
                }
                ++j;
            }
        }
    }

    private void renderSlotContents(final GuiGraphicsExtractor graphics,
                                    final int mouseX,
                                    final int mouseY,
                                    final int y,
                                    final int topHeight,
                                    final int rows) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        for (final Slot slot : menu.getAutocrafterSlots()) {
            if (isOutOfFrame(y, topHeight, rows, topPos + slot.y)) {
                continue;
            }
            final boolean hovering = mouseX >= slot.x + leftPos
                && mouseX < slot.x + leftPos + 16
                && mouseY >= slot.y + topPos
                && mouseY < slot.y + topPos + 16;
            if (slot.isActive() && hovering) {
                ClientPlatformUtil.renderSlotHighlightBack(graphics, slot.x, slot.y);
            }
            super.extractSlot(graphics, slot, mouseX, mouseY);
            if (slot.isActive() && hovering) {
                ClientPlatformUtil.renderSlotHighlightFront(graphics, slot.x, slot.y);
            }
        }
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractSlot(final GuiGraphicsExtractor graphics, final Slot slot, final int mouseX,
                               final int mouseY) {
        if (slot instanceof AutocrafterManagerSlot) {
            return;
        }
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    private static boolean isOutOfFrame(final int y,
                                        final int topHeight,
                                        final int rows,
                                        final int rowY) {
        return (rowY < y + topHeight - ROW_SIZE)
            || (rowY > y + topHeight + (ROW_SIZE * rows));
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphicsExtractor graphics, final int x, final int y,
                                              final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(GUI_TEXTURED, getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE, 256,
                256);
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
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return getMenu().containsPattern(stack);
    }
}
