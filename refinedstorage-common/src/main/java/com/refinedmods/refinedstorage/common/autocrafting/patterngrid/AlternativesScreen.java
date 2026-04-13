package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage.common.support.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage.common.support.amount.DoubleAmountOperations;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.widget.CheckboxWidget;
import com.refinedmods.refinedstorage.common.support.widget.CustomButton;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class AlternativesScreen extends AbstractAmountScreen<AlternativeContainerMenu, Double> {
    static final int ALTERNATIVE_ROW_HEIGHT = 18;
    static final int ALTERNATIVE_HEIGHT = ALTERNATIVE_ROW_HEIGHT * 2;
    static final int RESOURCES_PER_ROW = 9;

    private static final Identifier TEXTURE = createIdentifier("textures/gui/alternatives.png");
    private static final MutableComponent TITLE = createTranslation("gui", "configure_amount");
    private static final MutableComponent ALTERNATIVES = createTranslation("gui", "pattern_grid.alternatives");
    private static final MutableComponent EXPAND = createTranslation("gui", "pattern_grid.alternatives.expand");
    private static final Component SEARCH_HELP = createTranslation("gui", "pattern_grid.alternatives.search_help")
        .withStyle(ChatFormatting.GRAY);
    private static final WidgetSprites EXPAND_SPRITES = new WidgetSprites(
        createIdentifier("widget/expand"),
        createIdentifier("widget/expand_disabled"),
        createIdentifier("widget/expand_focused"),
        createIdentifier("widget/expand_disabled")
    );
    private static final WidgetSprites COLLAPSE_SPRITES = new WidgetSprites(
        createIdentifier("widget/collapse"),
        createIdentifier("widget/collapse_focused")
    );

    private static final int ALTERNATIVES_DISPLAYED = 2;
    private static final int ROWS_PER_ALTERNATIVE = 2;
    private static final int INSET_WIDTH = 164;
    private static final int INSET_HEIGHT = ALTERNATIVE_HEIGHT * ALTERNATIVES_DISPLAYED;

    private final ResourceSlot slot;

    @Nullable
    private ScrollbarWidget scrollbar;
    @Nullable
    private EditBox searchField;

    private final List<CheckboxWidget> alternativeCheckboxes = new ArrayList<>();
    private final List<Button> expandButtons = new ArrayList<>();
    private final Set<Identifier> initialAllowedAlternativeIds;

    AlternativesScreen(final Screen parent,
                       final Inventory playerInventory,
                       final Set<Identifier> allowedAlternativeIds,
                       final ResourceSlot slot) {
        super(
            new AlternativeContainerMenu(slot),
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(slot.getDisplayAmount())
                .withIncrementsTop(1, 10, 64)
                .withIncrementsTopStartPosition(new Vector3f(49, 20, 0))
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(49, 71, 0))
                .withAmountFieldPosition(new Vector3f(47, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(7, 199, 0))
                .withHorizontalActionButtons(true)
                .withMinAmount(() -> slot.getResource() != null
                    ? slot.getResource().getResourceType().getDisplayAmount(1)
                    : 1)
                .withMaxAmount(slot.getMaxAmountWhenModifying())
                .withResetAmount(1D)
                .build(),
            DoubleAmountOperations.INSTANCE,
            193,
            226
        );
        this.slot = slot;
        this.initialAllowedAlternativeIds = allowedAlternativeIds;
    }

    @Override
    protected void init() {
        super.init();
        alternativeCheckboxes.clear();
        expandButtons.clear();
        final int x = getInsetX();
        for (int i = 0; i < getMenu().getAlternatives().size(); ++i) {
            addWidgetsForAlternative(i, x);
        }
        scrollbar = new ScrollbarWidget(
            leftPos + 173,
            topPos + 122,
            ScrollbarWidget.Type.NORMAL,
            INSET_HEIGHT
        );
        final int overflowingAlternatives = getMenu().getAlternatives().size() - ALTERNATIVES_DISPLAYED;
        final int maxOffset = scrollbar.isSmoothScrolling()
            ? overflowingAlternatives * ALTERNATIVE_HEIGHT
            : overflowingAlternatives * ROWS_PER_ALTERNATIVE;
        scrollbar.setMaxOffset(maxOffset);
        scrollbar.setEnabled(maxOffset > 0);
        scrollbar.setListener(value -> updateWidgets());
        addWidget(scrollbar);
        searchField = new EditBox(
            font,
            leftPos + 24,
            topPos + 109,
            162 - 6,
            font.lineHeight,
            Component.empty()
        );
        searchField.setBordered(false);
        searchField.setVisible(true);
        searchField.setCanLoseFocus(true);
        searchField.setFocused(false);
        searchField.setResponder(query -> getMenu().filter(query));
        addRenderableWidget(searchField);

        addRenderableWidget(new SearchIconWidget(
            leftPos + 7,
            topPos + 107,
            () -> SEARCH_HELP,
            searchField
        ));
    }

    private int getInsetY() {
        return topPos + 122;
    }

    private int getInsetX() {
        return leftPos + 8;
    }

    private int getAlternativeY(final int idx) {
        return getInsetY() + (ALTERNATIVE_HEIGHT * idx);
    }

    private void addWidgetsForAlternative(final int idx, final int x) {
        final Alternative alternative = getMenu().getAlternatives().get(idx);
        final int y = getAlternativeY(idx);
        final boolean hasTranslation = I18n.exists(alternative.getTranslationKey());
        final MutableComponent id = Component.literal(alternative.getId().toString());
        final CheckboxWidget alternativeCheckbox = new CheckboxWidget(
            x + 2,
            y + (ALTERNATIVE_ROW_HEIGHT / 2) - (9 / 2),
            164 - 2 - 16 - 1 - 4,
            hasTranslation ? Component.translatable(alternative.getTranslationKey()) : id,
            font,
            initialAllowedAlternativeIds.contains(alternative.getId()),
            CheckboxWidget.Size.SMALL
        );
        if (hasTranslation) {
            alternativeCheckbox.setTooltip(Tooltip.create(id));
        }
        alternativeCheckboxes.add(addWidget(alternativeCheckbox));
        final CustomButton expandButton = new CustomButton(
            x + INSET_WIDTH - 16 - 1,
            y + 1,
            16,
            16,
            EXPAND_SPRITES,
            btn -> {
                final boolean expanding = alternative.expandOrCollapse();
                btn.setSprites(expanding ? COLLAPSE_SPRITES : EXPAND_SPRITES);
            },
            EXPAND
        );
        expandButton.active = alternative.getResources().size() > RESOURCES_PER_ROW;
        expandButtons.add(addWidget(expandButton));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        getMenu().getAlternatives().forEach(Alternative::update);
        updateWidgets();
    }

    private void updateWidgets() {
        final ScrollbarWidget theScrollbar = scrollbar;
        if (theScrollbar == null) {
            return;
        }
        double totalHeight = 0;
        int totalRows = 0;
        final int scrollbarOffset = (int) theScrollbar.getOffset();
        int y = getAlternativeY(0)
            - (theScrollbar.isSmoothScrolling() ? scrollbarOffset : scrollbarOffset * ALTERNATIVE_ROW_HEIGHT);
        for (int i = 0; i < getMenu().getAlternatives().size(); ++i) {
            final Alternative alternative = getMenu().getAlternatives().get(i);
            final CheckboxWidget alternativeCheckbox = alternativeCheckboxes.get(i);
            final Button expandButton = expandButtons.get(i);

            if (!alternative.isVisible()) {
                alternativeCheckbox.visible = false;
                expandButton.visible = false;
                updateAlternativeSlots(alternative.getMainSlots(), y, 0, false);
                updateAlternativeSlots(alternative.getOverflowSlots(), y, 1, false);
                continue;
            }

            totalRows += ROWS_PER_ALTERNATIVE;
            final int overflowRows = getOverflowRows(alternative);
            totalRows += (int) (overflowRows * alternative.getExpandPct());
            final int height = ALTERNATIVE_HEIGHT
                + (int) (overflowRows * ALTERNATIVE_ROW_HEIGHT * alternative.getExpandPct());

            updateAlternativeCheckbox(alternativeCheckbox, y);
            updateExpandButton(expandButton, y);
            updateAlternativeSlots(alternative.getMainSlots(), y, 0, true);
            updateAlternativeSlots(alternative.getOverflowSlots(), y, 1, alternative.getExpandPct() > 0);

            totalHeight += height;
            y += height;
        }
        final double maxOffset = theScrollbar.isSmoothScrolling()
            ? totalHeight - (ALTERNATIVE_HEIGHT * ALTERNATIVES_DISPLAYED)
            : totalRows - (ROWS_PER_ALTERNATIVE * ALTERNATIVES_DISPLAYED);
        theScrollbar.setMaxOffset(maxOffset);
        theScrollbar.setEnabled(maxOffset > 0);
    }

    private void updateAlternativeCheckbox(final CheckboxWidget alternativeCheckbox, final int y) {
        alternativeCheckbox.setY(y + (ALTERNATIVE_ROW_HEIGHT / 2) - (9 / 2));
        alternativeCheckbox.visible = alternativeCheckbox.getY() >= getInsetY() - alternativeCheckbox.getHeight()
            && alternativeCheckbox.getY() < getInsetY() + INSET_HEIGHT;
    }

    private void updateExpandButton(final Button expandButton, final int y) {
        expandButton.setY(y + 1);
        expandButton.visible = expandButton.getY() >= getInsetY() - expandButton.getHeight()
            && expandButton.getY() < getInsetY() + INSET_HEIGHT;
    }

    private void updateAlternativeSlots(final List<AlternativeSlot> slots,
                                        final int y,
                                        final int rowOffset,
                                        final boolean visible) {
        for (int i = 0; i < slots.size(); i++) {
            final int row = (i / RESOURCES_PER_ROW) + rowOffset;
            final AlternativeSlot resourceSlot = slots.get(i);
            Platform.INSTANCE.setSlotY(
                resourceSlot,
                (y + ALTERNATIVE_ROW_HEIGHT + (row * 18) + 1) - topPos
            );
            resourceSlot.setActive((resourceSlot.y + topPos) >= getInsetY() - 18
                && (resourceSlot.y + topPos) < getInsetY() + INSET_HEIGHT
                && visible);
        }
    }

    private static int getOverflowRows(final Alternative alternative) {
        return Math.ceilDiv(
            alternative.getResources().size() - RESOURCES_PER_ROW,
            RESOURCES_PER_ROW
        );
    }

    @Override
    protected void renderResourceSlots(final GuiGraphicsExtractor graphics) {
        ResourceSlotRendering.render(graphics, getMenu().getAmountSlot());
    }

    @Override
    protected boolean canInteractWithResourceSlot(final ResourceSlot resourceSlot,
                                                  final double mouseX,
                                                  final double mouseY) {
        final int insetContentX = getInsetX();
        final int insetContentY = getInsetY();
        return mouseX >= insetContentX
            && mouseX < insetContentX + INSET_WIDTH
            && mouseY >= insetContentY
            && mouseY < insetContentY + INSET_HEIGHT;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (scrollbar != null) {
            scrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        final int x = getInsetX();
        final int y = getInsetY();
        graphics.enableScissor(x, y, x + INSET_WIDTH, y + INSET_HEIGHT);
        int currentY = y - ((scrollbar != null ? (int) scrollbar.getOffset() : 0)
            * (scrollbar != null && scrollbar.isSmoothScrolling() ? 1 : ALTERNATIVE_ROW_HEIGHT));
        for (int i = 0; i < getMenu().getAlternatives().size(); ++i) {
            currentY += renderAlternativeBackground(graphics, mouseX, mouseY, i, y, x, currentY);
        }
        renderAlternativeMainSlots(graphics, mouseX, mouseY);
        alternativeCheckboxes.forEach(c -> c.extractRenderState(graphics, mouseX, mouseY, partialTicks));
        expandButtons.forEach(c -> c.extractRenderState(graphics, mouseX, mouseY, partialTicks));
        graphics.disableScissor();
    }

    private int renderAlternativeBackground(
        final GuiGraphicsExtractor graphics,
        final int mouseX,
        final int mouseY,
        final int i,
        final int startY,
        final int x,
        final int y
    ) {
        final Alternative alternative = getMenu().getAlternatives().get(i);
        if (!alternative.isVisible()) {
            return 0;
        }
        final int height = ALTERNATIVE_HEIGHT
            + (int) (getOverflowRows(alternative) * ALTERNATIVE_ROW_HEIGHT * alternative.getExpandPct());
        final boolean backgroundVisible = y >= startY - height && y < startY + INSET_HEIGHT;
        if (i % 2 == 0 && backgroundVisible) {
            graphics.fill(
                x,
                y,
                x + INSET_WIDTH,
                y + height,
                0xFFC6C6C6
            );
        }
        final int mainSlotsY = y + ALTERNATIVE_ROW_HEIGHT;
        renderMainSlotsBackground(graphics, startY, x, mainSlotsY, alternative);
        final int overflowSlotsY = y + (ALTERNATIVE_ROW_HEIGHT * 2);
        return ALTERNATIVE_HEIGHT + renderOverflowSlotsBackground(
            graphics,
            mouseX,
            mouseY,
            startY,
            x,
            overflowSlotsY,
            alternative
        );
    }

    private void renderMainSlotsBackground(
        final GuiGraphicsExtractor graphics,
        final int startY,
        final int x,
        final int y,
        final Alternative alternative
    ) {
        if (y >= startY - ALTERNATIVE_ROW_HEIGHT && y < startY + INSET_HEIGHT) {
            for (int col = 0; col < Math.min(alternative.getResources().size(), RESOURCES_PER_ROW); ++col) {
                final int slotX = x + 1 + (col * 18);
                graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, slotX, y, 18, 18);
            }
        }
    }

    private int renderOverflowSlotsBackground(final GuiGraphicsExtractor graphics,
                                              final int mouseX,
                                              final int mouseY,
                                              final int startY,
                                              final int x,
                                              final int y,
                                              final Alternative alternative) {
        final int rows = getOverflowRows(alternative);
        final int height = (int) (rows * ALTERNATIVE_ROW_HEIGHT * alternative.getExpandPct());
        if (height == 0) {
            return 0;
        }
        graphics.enableScissor(x, y, x + (18 * RESOURCES_PER_ROW), y + height);
        for (int row = 0; row < rows; ++row) {
            final int rowY = y + (ALTERNATIVE_ROW_HEIGHT * row);
            final boolean visible = rowY >= startY - ALTERNATIVE_ROW_HEIGHT && rowY < startY + INSET_HEIGHT;
            if (!visible) {
                continue;
            }
            for (int col = 0; col < RESOURCES_PER_ROW; ++col) {
                final int idx = RESOURCES_PER_ROW + (row * RESOURCES_PER_ROW) + col;
                if (idx >= alternative.getResources().size()) {
                    break;
                }
                final int slotX = x + 1 + (col * 18);
                graphics.blitSprite(GUI_TEXTURED, Sprites.SLOT, slotX, rowY, 18, 18);
            }
        }
        renderSlots(alternative.getOverflowSlots(), graphics, mouseX, mouseY);
        graphics.disableScissor();
        return height;
    }

    private void renderAlternativeMainSlots(final GuiGraphicsExtractor graphics,
                                            final int mouseX,
                                            final int mouseY) {
        for (final Alternative alternative : getMenu().getAlternatives()) {
            renderSlots(alternative.getMainSlots(), graphics, mouseX, mouseY);
        }
    }

    private void renderSlots(final List<AlternativeSlot> slots,
                             final GuiGraphicsExtractor graphics,
                             final int mouseX,
                             final int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        for (final ResourceSlot resourceSlot : slots) {
            renderSlot(graphics, mouseX, mouseY, resourceSlot);
        }
        graphics.pose().popMatrix();
    }

    private void renderSlot(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                            final ResourceSlot resourceSlot) {
        if (!resourceSlot.isActive()) {
            return;
        }
        final boolean hovering = isHovering(resourceSlot.x, resourceSlot.y, 16, 16, mouseX, mouseY)
            && canInteractWithResourceSlot(resourceSlot, mouseX, mouseY);
        if (hovering) {
            ClientPlatformUtil.renderSlotHighlightBack(graphics, resourceSlot.x, resourceSlot.y);
        }
        ResourceSlotRendering.render(graphics, resourceSlot);
        if (hovering) {
            ClientPlatformUtil.renderSlotHighlightFront(graphics, resourceSlot.x, resourceSlot.y);
        }
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, ALTERNATIVES, 7, 96, -12566464, false);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        return (scrollbar != null && scrollbar.mouseClicked(event, doubleClick))
            || super.mouseClicked(event, doubleClick);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        return (scrollbar != null && scrollbar.mouseReleased(event))
            || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        final boolean didScrollbar = isOverAlternativesArea(x, y)
            && scrollbar != null
            && scrollbar.mouseScrolled(x, y, scrollX, scrollY);
        return didScrollbar || super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return (searchField != null && searchField.charTyped(event))
            || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (tryClose(event.key())) {
            return true;
        }
        if (searchField != null
            && (searchField.keyPressed(event) || searchField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean isOverAlternativesArea(final double x, final double y) {
        return x >= leftPos + 7
            && (x < leftPos + 7 + 179)
            && y >= topPos + 121
            && (y < topPos + 121 + 74);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected boolean confirm(final Double amount) {
        slot.changeAmountOnClient(amount);
        final Set<Alternative> allowedAlternatives = new HashSet<>();
        for (int i = 0; i < alternativeCheckboxes.size(); ++i) {
            if (alternativeCheckboxes.get(i).isSelected()) {
                allowedAlternatives.add(getMenu().getAlternatives().get(i));
            }
        }
        getMenu().sendAllowedAlternatives(allowedAlternatives);
        return true;
    }

    @Override
    protected void reset() {
        super.reset();
        alternativeCheckboxes.forEach(c -> c.setSelected(false));
    }
}
