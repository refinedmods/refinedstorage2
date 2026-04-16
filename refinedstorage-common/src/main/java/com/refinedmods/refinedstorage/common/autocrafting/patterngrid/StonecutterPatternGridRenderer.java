package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_PADDING;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class StonecutterPatternGridRenderer implements PatternGridRenderer {
    private static final Identifier STONECUTTER_RECIPE_SELECTED_SPRITE = Identifier.withDefaultNamespace(
        "container/stonecutter/recipe_selected"
    );
    private static final Identifier STONECUTTER_RECIPE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace(
        "container/stonecutter/recipe_highlighted"
    );
    private static final Identifier STONECUTTER_RECIPE_SPRITE = Identifier.withDefaultNamespace(
        "container/stonecutter/recipe"
    );
    private static final int STONECUTTER_RECIPES_PER_ROW = 4;
    private static final int STONECUTTER_ROWS_VISIBLE = 3;
    private static final Identifier SPRITE = createIdentifier("pattern_grid/stonecutter");

    @Nullable
    private ScrollbarWidget scrollbar;

    private final PatternGridContainerMenu menu;
    private final int leftPos;
    private final int x;
    private final int y;

    StonecutterPatternGridRenderer(final PatternGridContainerMenu menu, final int leftPos, final int x, final int y) {
        this.menu = menu;
        this.leftPos = leftPos;
        this.x = x;
        this.y = y;
    }

    @Override
    public void addWidgets(final Consumer<AbstractWidget> widgets,
                           final Consumer<AbstractWidget> renderables) {
        scrollbar = createStonecutterScrollbar(menu, x, y);
        updateScrollbarMaxOffset();
        widgets.accept(scrollbar);
    }

    private static ScrollbarWidget createStonecutterScrollbar(final PatternGridContainerMenu menu,
                                                              final int x,
                                                              final int y) {
        final ScrollbarWidget scrollbar = new ScrollbarWidget(
            x + 107,
            y + 9,
            ScrollbarWidget.Type.NORMAL,
            54
        );
        scrollbar.visible = isScrollbarVisible(menu);
        return scrollbar;
    }

    private void updateScrollbarMaxOffset() {
        if (scrollbar == null) {
            return;
        }
        final int items = menu.getStonecutterRecipes().size();
        final int rows = Math.ceilDiv(items, STONECUTTER_RECIPES_PER_ROW);
        final int maxOffset = rows - STONECUTTER_ROWS_VISIBLE;
        final int maxOffsetCorrected = maxOffset * (scrollbar.isSmoothScrolling() ? 18 : 1);
        scrollbar.setMaxOffset(maxOffsetCorrected);
        scrollbar.setEnabled(maxOffsetCorrected > 0);
    }

    @Override
    public void tick() {
        updateScrollbarMaxOffset();
    }

    @Override
    public void render(final GuiGraphicsExtractor graphics,
                       final int mouseX,
                       final int mouseY,
                       final float partialTicks) {
        if (scrollbar != null) {
            scrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public int getClearButtonX() {
        return leftPos + 130;
    }

    @Override
    public int getClearButtonY() {
        return y + 8;
    }

    @Override
    public void renderBackground(final GuiGraphicsExtractor graphics,
                                 final float partialTicks,
                                 final int mouseX,
                                 final int mouseY) {
        final Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        graphics.blitSprite(GUI_TEXTURED, SPRITE, x + INSET_PADDING, y + INSET_PADDING + 4, 116, 56);
        graphics.enableScissor(x + 40, y + 9, x + 40 + 64, y + 9 + 54);
        final boolean isOverArea = isOverStonecutterArea(mouseX, mouseY);
        final ContextMap context = SlotDisplayContext.fromLevel(level);
        for (int i = 0; i < menu.getStonecutterRecipes().size(); ++i) {
            renderButton(graphics, mouseX, mouseY, i, isOverArea, context);
        }
        graphics.disableScissor();
    }

    private void renderButton(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final int i,
                              final boolean isOverArea, final ContextMap context) {
        final SelectableRecipe.SingleInputEntry<StonecutterRecipe> recipe =
            menu.getStonecutterRecipes().entries().get(i);
        final int xx = getRecipeX(x, i);
        final int row = i / STONECUTTER_RECIPES_PER_ROW;
        final int yy = getRecipeY(y, row);
        if (yy < y + 9 - 18 || yy > y + 9 + 54) {
            return;
        }
        final boolean hovering = mouseX >= xx && mouseY >= yy && mouseX < xx + 16 && mouseY < yy + 18;
        final Identifier buttonSprite;
        if (i == menu.getStonecutterSelectedRecipe()) {
            buttonSprite = STONECUTTER_RECIPE_SELECTED_SPRITE;
        } else if (isOverArea && hovering) {
            buttonSprite = STONECUTTER_RECIPE_HIGHLIGHTED_SPRITE;
        } else {
            buttonSprite = STONECUTTER_RECIPE_SPRITE;
        }
        graphics.blitSprite(GUI_TEXTURED, buttonSprite, xx, yy, 16, 18);
        final ItemStack output = recipe.recipe().optionDisplay().resolveForFirstStack(context);
        graphics.item(output, xx, yy + 1);
        ResourceSlotRendering.renderAmount(graphics, xx - 1, yy + 1, output.count(),
            RefinedStorageClientApi.INSTANCE.getResourceRendering(ItemResource.class));
    }

    @Override
    public void renderTooltip(final Font font,
                              @Nullable final Slot hoveredSlot,
                              final GuiGraphicsExtractor graphics,
                              final int mouseX,
                              final int mouseY) {
        final Level level = Minecraft.getInstance().level;
        if (level == null || !isOverStonecutterArea(mouseX, mouseY)) {
            return;
        }
        final ContextMap context = SlotDisplayContext.fromLevel(level);
        for (int i = 0; i < menu.getStonecutterRecipes().size(); ++i) {
            final SelectableRecipe.SingleInputEntry<StonecutterRecipe> recipe =
                menu.getStonecutterRecipes().entries().get(i);
            final ItemStack result = recipe.recipe().optionDisplay().resolveForFirstStack(context);
            final int xx = getRecipeX(x, i);
            final int row = i / STONECUTTER_RECIPES_PER_ROW;
            final int yy = getRecipeY(y, row);
            if (yy < y + 9 - 18 || yy > y + 9 + 54) {
                continue;
            }
            if (mouseX >= xx && mouseY >= yy && mouseX < xx + 16 && mouseY < yy + 18) {
                graphics.setTooltipForNextFrame(font, Screen.getTooltipFromItem(Minecraft.getInstance(),
                    result), result.getTooltipImage(), mouseX, mouseY, result.get(DataComponents.TOOLTIP_STYLE));
            }
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent e, final boolean doubleClick) {
        return (scrollbar != null && scrollbar.mouseClicked(e, doubleClick)) || clickedRecipe(e.x(), e.y());
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent e) {
        return scrollbar != null && scrollbar.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(final double mouseX,
                                 final double mouseY,
                                 final double mouseZ,
                                 final double delta) {
        return isOverStonecutterArea(mouseX, mouseY)
            && scrollbar != null
            && scrollbar.isActive()
            && scrollbar.mouseScrolled(mouseX, mouseY, mouseZ, delta);
    }

    @Override
    public void patternTypeChanged(final PatternType newPatternType) {
        if (scrollbar != null) {
            scrollbar.visible = isScrollbarVisible(menu);
        }
    }

    private static boolean isScrollbarVisible(final PatternGridContainerMenu menu) {
        return menu.getPatternType() == PatternType.STONECUTTER;
    }

    private boolean clickedRecipe(final double mouseX,
                                  final double mouseY) {
        if (!isOverStonecutterArea(mouseX, mouseY)) {
            return false;
        }
        for (int i = 0; i < menu.getStonecutterRecipes().size(); ++i) {
            final int xx = getRecipeX(x, i);
            final int row = i / STONECUTTER_RECIPES_PER_ROW;
            final int yy = getRecipeY(y, row);
            if (yy < y + 9 - 18 || yy > y + 9 + 54) {
                continue;
            }
            if (mouseX >= xx && mouseY >= yy && mouseX < xx + 16 && mouseY < yy + 18) {
                menu.setStonecutterSelectedRecipe(i);
                Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
                );
                return true;
            }
        }
        return false;
    }

    private boolean isOverStonecutterArea(final double mouseX, final double mouseY) {
        return mouseX >= x + 40
            && (mouseX < x + 40 + 81)
            && mouseY > y + 8
            && (mouseY < y + 8 + 56);
    }

    private int getRecipeX(final int insetX, final int i) {
        return insetX + 40 + i % STONECUTTER_RECIPES_PER_ROW * 16;
    }

    private int getRecipeY(final int insetY, final int row) {
        return insetY
            + 9
            + (row * 18)
            - ((scrollbar != null ? (int) scrollbar.getOffset() : 0)
            * (scrollbar != null && scrollbar.isSmoothScrolling() ? 1 : 18));
    }
}
