package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;

import java.awt.Color;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

class CraftingGridTransferHandler implements TransferHandler {
    private static final Color MISSING_COLOR = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    private static final CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");

    @Override
    public Result handle(final Context context) {
        if (!(context.getMenu() instanceof CraftingGridContainerMenu containerMenu)
            || !context.getDisplay().getCategoryIdentifier().equals(CRAFTING)
            || !(context.getDisplay() instanceof DefaultCraftingDisplay<?> defaultCraftingDisplay)) {
            return Result.createNotApplicable();
        }
        final List<EntryIngredient> ingredients = defaultCraftingDisplay.getOrganisedInputEntries(3, 3);
        if (context.isActuallyCrafting()) {
            doTransfer(ingredients, containerMenu);
            return Result.createSuccessful().blocksFurtherHandling();
        }
        final ResourceList available = containerMenu.getAvailableListForRecipeTransfer();
        final MissingIngredients missingIngredients = findMissingIngredients(ingredients, available);
        if (missingIngredients.isEmpty()) {
            return Result.createSuccessful().blocksFurtherHandling();
        }
        return Result.createSuccessful()
            .color(MISSING_COLOR.getRGB())
            .tooltipMissing(missingIngredients.getIngredients())
            .renderer(createMissingItemsRenderer(missingIngredients))
            .blocksFurtherHandling();
    }

    private void doTransfer(final List<EntryIngredient> ingredients, final CraftingGridContainerMenu containerMenu) {
        final List<List<ItemResource>> inputs = getInputs(ingredients);
        containerMenu.transferRecipe(inputs);
    }

    private MissingIngredients findMissingIngredients(final List<EntryIngredient> ingredients,
                                                      final ResourceList available) {
        final MissingIngredients missingIngredients = new MissingIngredients();
        for (int i = 0; i < ingredients.size(); ++i) {
            final EntryIngredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            if (!isAvailable(available, ingredient)) {
                missingIngredients.addIngredient(ingredient, i);
            }
        }
        return missingIngredients;
    }

    private boolean isAvailable(final ResourceList available, final EntryIngredient ingredient) {
        final List<ItemStack> possibilities = convertIngredientToItemStacks(ingredient);
        for (final ItemStack possibility : possibilities) {
            final ItemResource possibilityResource = ItemResource.ofItemStack(possibility);
            if (available.remove(possibilityResource, 1).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private List<List<ItemResource>> getInputs(final List<EntryIngredient> ingredients) {
        return ingredients.stream()
            .map(this::convertIngredientToItemStacks)
            .map(list -> list.stream().map(ItemResource::ofItemStack).toList())
            .toList();
    }

    private List<ItemStack> convertIngredientToItemStacks(final EntryIngredient ingredient) {
        return CollectionUtils.<EntryStack<?>, ItemStack>filterAndMap(
            ingredient,
            stack -> stack.getType() == VanillaEntryTypes.ITEM,
            EntryStack::castValue
        );
    }

    private TransferHandlerRenderer createMissingItemsRenderer(final MissingIngredients missingIngredients) {
        return (graphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int index = 0;
            for (final Widget widget : widgets) {
                if (widget instanceof Slot slot
                    && slot.getNoticeMark() == Slot.INPUT
                    && missingIngredients.isMissing(index++)) {
                    renderMissingItemOverlay(graphics, slot);
                }
            }
        };
    }

    private void renderMissingItemOverlay(final GuiGraphics graphics, final Slot slot) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);
        final Rectangle innerBounds = slot.getInnerBounds();
        graphics.fill(
            innerBounds.x,
            innerBounds.y,
            innerBounds.getMaxX(),
            innerBounds.getMaxY(),
            MISSING_COLOR.getRGB()
        );
        poseStack.popPose();
    }
}
