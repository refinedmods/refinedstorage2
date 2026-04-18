package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.autocrafting.VanillaConstants;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_PADDING;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

class StonecutterPatternGridRenderer implements PatternGridRenderer {
    private static final ResourceLocation SPRITE = createIdentifier("pattern_grid/stonecutter");

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
        final int rows = Math.ceilDiv(items, VanillaConstants.STONECUTTER_RECIPES_PER_ROW);
        final int maxOffset = rows - VanillaConstants.STONECUTTER_ROWS_VISIBLE;
        final int maxOffsetCorrected = maxOffset * (scrollbar.isSmoothScrolling() ? 18 : 1);
        scrollbar.setMaxOffset(maxOffsetCorrected);
        scrollbar.setEnabled(maxOffsetCorrected > 0);
    }

    @Override
    public void tick() {
        updateScrollbarMaxOffset();
    }

    @Override
    public void render(final GuiGraphics graphics,
                       final int mouseX,
                       final int mouseY,
                       final float partialTicks) {
        if (scrollbar != null) {
            scrollbar.render(graphics, mouseX, mouseY, partialTicks);
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
    public void renderBackground(final GuiGraphics graphics,
                                 final float partialTicks,
                                 final int mouseX,
                                 final int mouseY) {
        graphics.blitSprite(SPRITE, x + INSET_PADDING, y + INSET_PADDING + 4, 116, 56);
        graphics.enableScissor(x + 40, y + 9, x + 40 + 64, y + 9 + 54);
        final boolean isOverArea = isOverStonecutterArea(mouseX, mouseY);
        for (int i = 0; i < menu.getStonecutterRecipes().size(); ++i) {
            final RecipeHolder<StonecutterRecipe> recipe = menu.getStonecutterRecipes().get(i);
            final int xx = getRecipeX(x, i);
            final int row = i / VanillaConstants.STONECUTTER_RECIPES_PER_ROW;
            final int yy = getRecipeY(y, row);
            if (yy < y + 9 - 18 || yy > y + 9 + 54) {
                continue;
            }
            final boolean hovering = mouseX >= xx && mouseY >= yy && mouseX < xx + 16 && mouseY < yy + 18;
            final ResourceLocation buttonSprite;
            if (i == menu.getStonecutterSelectedRecipe()) {
                buttonSprite = VanillaConstants.STONECUTTER_RECIPE_SELECTED_SPRITE;
            } else if (isOverArea && hovering) {
                buttonSprite = VanillaConstants.STONECUTTER_RECIPE_HIGHLIGHTED_SPRITE;
            } else {
                buttonSprite = VanillaConstants.STONECUTTER_RECIPE_SPRITE;
            }
            graphics.blitSprite(buttonSprite, xx, yy, 16, 18);
            final ItemStack output =
                recipe.value().getResultItem(requireNonNull(ClientPlatformUtil.getClientLevel()).registryAccess());
            graphics.renderItem(
                output,
                xx,
                yy + 1
            );
            ResourceSlotRendering.renderAmount(graphics, xx - 1, yy + 1, output.getCount(),
                RefinedStorageClientApi.INSTANCE.getResourceRendering(ItemResource.class));
        }
        graphics.disableScissor();
    }

    @Override
    public void renderTooltip(final Font font,
                              @Nullable final Slot hoveredSlot,
                              final GuiGraphics graphics,
                              final int mouseX,
                              final int mouseY) {
        if (!isOverStonecutterArea(mouseX, mouseY)) {
            return;
        }
        for (int i = 0; i < menu.getStonecutterRecipes().size(); ++i) {
            final RecipeHolder<StonecutterRecipe> recipe = menu.getStonecutterRecipes().get(i);
            final ItemStack result = recipe.value().getResultItem(
                requireNonNull(ClientPlatformUtil.getClientLevel()).registryAccess()
            );
            final int xx = getRecipeX(x, i);
            final int row = i / VanillaConstants.STONECUTTER_RECIPES_PER_ROW;
            final int yy = getRecipeY(y, row);
            if (yy < y + 9 - 18 || yy > y + 9 + 54) {
                continue;
            }
            if (mouseX >= xx && mouseY >= yy && mouseX < xx + 16 && mouseY < yy + 18) {
                graphics.renderTooltip(font, result, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        return (scrollbar != null && scrollbar.mouseClicked(mouseX, mouseY, clickedButton))
            || clickedRecipe(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        if (scrollbar != null) {
            scrollbar.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        return scrollbar != null && scrollbar.mouseReleased(mouseX, mouseY, button);
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
            final int row = i / VanillaConstants.STONECUTTER_RECIPES_PER_ROW;
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
        return insetX + 40 + i % VanillaConstants.STONECUTTER_RECIPES_PER_ROW * 16;
    }

    private int getRecipeY(final int insetY, final int row) {
        return insetY
            + 9
            + (row * 18)
            - ((scrollbar != null ? (int) scrollbar.getOffset() : 0)
            * (scrollbar != null && scrollbar.isSmoothScrolling() ? 1 : 18));
    }
}
