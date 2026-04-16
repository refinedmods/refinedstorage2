package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.FilterSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

public class PatternGridContainerMenu extends AbstractGridContainerMenu {
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_PATTERN_INPUT_SLOT = 81;
    private static final int SPACING_BETWEEN_PATTERN_INPUT_AND_PATTERN_OUTPUT_SLOTS = 36;
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT = 85;
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_PROCESSING_MATRIX_SLOT = 76;
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_STONECUTTER_SLOT = 63;
    private static final int Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_SMITHING_TABLE_SLOTS = 63;
    private static final int INDIVIDUAL_PROCESSING_MATRIX_SIZE = 54;

    private final Container patternInput;
    private final Container patternOutput;
    private final Container craftingMatrix;
    private final Container craftingResult;
    private final ProcessingMatrixInputResourceContainer processingInput;
    private final ResourceContainer processingOutput;
    private final StonecutterInputContainer stonecutterInput;
    private final Container smithingTableMatrix;
    private final Container smithingTableResult;
    private final RecipePropertySet smithingBaseItemTest;
    private final RecipePropertySet smithingTemplateItemTest;
    private final RecipePropertySet smithingAdditionItemTest;

    @Nullable
    private PatternGridListener listener;
    @Nullable
    private PatternGridBlockEntity patternGrid;
    @Nullable
    private Slot patternOutputSlot;

    public PatternGridContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final PatternGridData patternGridData) {
        super(Menus.INSTANCE.getPatternGrid(), syncId, playerInventory, patternGridData.gridData());
        this.patternInput = new FilteredContainer(1, PatternGridBlockEntity::isValidPattern);
        this.patternOutput = new PatternOutputContainer();
        this.processingInput = PatternGridBlockEntity.createProcessingMatrixInputContainer(
            patternGridData.processingInputData()
        );
        this.processingOutput = PatternGridBlockEntity.createProcessingMatrixOutputContainer(
            patternGridData.processingOutputData()
        );
        this.craftingMatrix = new RecipeMatrixContainer(null, 3, 3);
        this.craftingResult = new ResultContainer();
        this.stonecutterInput = new StonecutterInputContainer(playerInventory.player::level);
        this.smithingTableMatrix = new RecipeMatrixContainer(null, 3, 1);
        this.smithingTableResult = new ResultContainer();
        final RecipeAccess recipeAccess = playerInventory.player.level().recipeAccess();
        this.smithingBaseItemTest = recipeAccess.propertySet(RecipePropertySet.SMITHING_BASE);
        this.smithingTemplateItemTest = recipeAccess.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        this.smithingAdditionItemTest = recipeAccess.propertySet(RecipePropertySet.SMITHING_ADDITION);
        resized(0, 0, 0);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PatternGridPropertyTypes.PATTERN_TYPE, patternGridData.patternType()) {
            @Override
            protected void onChangedOnClient(final PatternType newValue) {
                super.onChangedOnClient(newValue);
                if (listener != null) {
                    listener.patternTypeChanged(newValue);
                }
            }
        });
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false) {
            @Override
            protected void onChangedOnClient(final Boolean newValue) {
                super.onChangedOnClient(newValue);
                if (listener != null) {
                    listener.fuzzyModeChanged(newValue);
                }
            }
        });
        registerProperty(new ClientProperty<>(
            PatternGridPropertyTypes.STONECUTTER_SELECTED_RECIPE,
            patternGridData.stonecutterSelectedRecipe()
        ));
    }

    PatternGridContainerMenu(final int syncId,
                             final Inventory playerInventory,
                             final PatternGridBlockEntity grid) {
        super(Menus.INSTANCE.getPatternGrid(), syncId, playerInventory, grid);
        this.patternInput = grid.getPatternInput();
        this.patternOutput = grid.getPatternOutput();
        this.craftingMatrix = grid.getCraftingMatrix();
        this.craftingResult = grid.getCraftingResult();
        this.stonecutterInput = grid.getStonecutterInput();
        this.processingInput = grid.getProcessingInput();
        this.processingOutput = grid.getProcessingOutput();
        this.smithingTableMatrix = grid.getSmithingTableMatrix();
        this.smithingTableResult = grid.getSmithingTableResult();
        final Level level = playerInventory.player.level();
        this.smithingBaseItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE);
        this.smithingTemplateItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        this.smithingAdditionItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
        this.patternGrid = grid;
        resized(0, 0, 0);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            grid::getRedstoneMode,
            grid::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PatternGridPropertyTypes.PATTERN_TYPE,
            grid::getPatternType,
            grid::setPatternType
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            grid::isFuzzyMode,
            grid::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PatternGridPropertyTypes.STONECUTTER_SELECTED_RECIPE,
            grid::getStonecutterSelectedRecipe,
            grid::setStonecutterSelectedRecipe
        ));
    }

    void setListener(final PatternGridListener listener) {
        this.listener = listener;
    }

    PatternType getPatternType() {
        return getProperty(PatternGridPropertyTypes.PATTERN_TYPE).getValue();
    }

    void setPatternType(final PatternType patternType) {
        getProperty(PatternGridPropertyTypes.PATTERN_TYPE).setValue(patternType);
    }

    boolean isFuzzyMode() {
        return Boolean.TRUE.equals(getProperty(PropertyTypes.FUZZY_MODE).getValue());
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        getProperty(PropertyTypes.FUZZY_MODE).setValue(fuzzyMode);
    }

    boolean canCreatePattern() {
        if (patternInput.getItem(0).isEmpty() && patternOutput.getItem(0).isEmpty()) {
            return false;
        }
        return switch (getPatternType()) {
            case CRAFTING -> !craftingResult.getItem(0).isEmpty();
            case PROCESSING -> !processingInput.isEmpty() && !processingOutput.isEmpty();
            case STONECUTTER -> !stonecutterInput.getItem(0).isEmpty() && getStonecutterSelectedRecipe() >= 0;
            case SMITHING_TABLE -> !smithingTableResult.getItem(0).isEmpty();
        };
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        super.resized(playerInventoryY, topYStart, topYEnd);
        transferManager.clear();
        addSmithingTableSlots(playerInventoryY); // these must be always first for the smithing table helpers
        addPatternSlots(playerInventoryY);
        addCraftingMatrixSlots(playerInventoryY);
        addProcessingMatrixSlots(playerInventoryY);
        addStonecutterSlots(playerInventoryY);
    }

    int getFirstSmithingTableSlotIndex() {
        return 9 * 3 + 9;
    }

    private void addPatternSlots(final int playerInventoryY) {
        addSlot(new ValidatedSlot(
            patternInput,
            0,
            152,
            playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_PATTERN_INPUT_SLOT,
            PatternGridBlockEntity::isValidPattern
        ));
        patternOutputSlot = addSlot(new ValidatedSlot(
            patternOutput,
            0,
            152,
            playerInventoryY
                - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_PATTERN_INPUT_SLOT
                + SPACING_BETWEEN_PATTERN_INPUT_AND_PATTERN_OUTPUT_SLOTS,
            PatternGridBlockEntity::isValidPattern
        ) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return patternOutput.canPlaceItem(0, stack);
            }

            @Override
            public void set(final ItemStack stack) {
                super.set(stack);
                if (patternGrid != null && !stack.isEmpty()) {
                    patternGrid.copyPattern(stack);
                }
            }
        });
        transferManager.addBiTransfer(playerInventory, patternInput);
        transferManager.addTransfer(patternOutput, playerInventory);
    }

    public boolean isPatternInOutput(final ItemStack stack) {
        return patternOutputSlot != null && patternOutputSlot.getItem() == stack;
    }

    @Nullable
    @Override
    protected ResourceKey getResourceForAutocraftableHint(final Slot slot) {
        final boolean isInputItem = slot.container == craftingMatrix
            || slot.container == stonecutterInput
            || slot.container == smithingTableMatrix;
        final boolean isResultItem = slot.container == craftingResult
            || slot.container == smithingTableResult;
        if (isInputItem || isResultItem) {
            return ItemResource.ofItemStack(slot.getItem());
        } else if (slot instanceof ProcessingMatrixResourceSlot processingMatrixSlot) {
            return processingMatrixSlot.getResource();
        }
        return super.getResourceForAutocraftableHint(slot);
    }

    @Override
    public boolean isLargeSlot(final Slot slot) {
        return slot.container == craftingResult || super.isLargeSlot(slot);
    }

    private void addCraftingMatrixSlots(final int playerInventoryY) {
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                final int slotX = 13 + ((x % 3) * 18);
                final int slotY = playerInventoryY
                    - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT
                    + ((y % 3) * 18);
                addSlot(new FilterSlot(craftingMatrix, x + y * 3, slotX, slotY) {
                    @Override
                    public boolean isActive() {
                        return getPatternType() == PatternType.CRAFTING;
                    }
                });
            }
        }
        addSlot(new DisabledSlot(
            craftingResult,
            0,
            117 + 4,
            playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_CRAFTING_MATRIX_SLOT + 18
        ) {
            @Override
            public boolean isActive() {
                return getPatternType() == PatternType.CRAFTING;
            }
        });
    }

    private void addProcessingMatrixSlots(final int playerInventoryY) {
        final int y = playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_FIRST_PROCESSING_MATRIX_SLOT;
        final int startY = y - 18;
        final int endY = y + INDIVIDUAL_PROCESSING_MATRIX_SIZE;
        addProcessingMatrixSlots(13, y, startY, endY, processingInput, true);
        addProcessingMatrixSlots(13 + INDIVIDUAL_PROCESSING_MATRIX_SIZE + 2, y, startY, endY, processingOutput, false);
    }

    private void addProcessingMatrixSlots(final int x,
                                          final int y,
                                          final int startY,
                                          final int endY,
                                          final ResourceContainer resourceContainer,
                                          final boolean input) {
        int slotX = x;
        int slotY = y;
        for (int i = 0; i < resourceContainer.size(); ++i) {
            //noinspection SuspiciousNameCombination
            addSlot(new ProcessingMatrixResourceSlot(
                resourceContainer,
                i,
                slotX,
                slotY,
                input,
                this::getPatternType,
                Pair.of(startY, endY)
            ));
            if ((i + 1) % 3 == 0) {
                slotX = x;
                slotY += 18;
            } else {
                slotX += 18;
            }
        }
    }

    private void addStonecutterSlots(final int playerInventoryY) {
        final int slotY = playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_STONECUTTER_SLOT;
        addSlot(new FilterSlot(stonecutterInput, 0, 13, slotY) {
            @Override
            public boolean isActive() {
                return getPatternType() == PatternType.STONECUTTER;
            }
        });
    }

    private void addSmithingTableSlots(final int playerInventoryY) {
        final int y = playerInventoryY - Y_OFFSET_BETWEEN_PLAYER_INVENTORY_AND_SMITHING_TABLE_SLOTS;
        for (int i = 0; i < 3; ++i) {
            final int ii = i;
            addSlot(new FilterSlot(smithingTableMatrix, i, 13 + (i * 18), y) {
                @Override
                public boolean isActive() {
                    return getPatternType() == PatternType.SMITHING_TABLE;
                }

                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return switch (ii) {
                        case 0 -> smithingTemplateItemTest.test(stack);
                        case 1 -> smithingBaseItemTest.test(stack);
                        case 2 -> smithingAdditionItemTest.test(stack);
                        default -> false;
                    };
                }
            });
        }
        addSlot(new DisabledSlot(smithingTableResult, 0, 93, y) {
            @Override
            public boolean isActive() {
                return getPatternType() == PatternType.SMITHING_TABLE;
            }
        });
    }

    Optional<SmithingTemplateItem> getSmithingTableTemplateItem() {
        final ItemStack stack = getSlot(getFirstSmithingTableSlotIndex()).getItem();
        if (!stack.isEmpty()) {
            final Item item = stack.getItem();
            if (item instanceof SmithingTemplateItem templateItem) {
                return Optional.of(templateItem);
            }
        }
        return Optional.empty();
    }

    SelectableRecipe.SingleInputSet<StonecutterRecipe> getStonecutterRecipes() {
        return stonecutterInput.getRecipes();
    }

    int getStonecutterSelectedRecipe() {
        return getProperty(PatternGridPropertyTypes.STONECUTTER_SELECTED_RECIPE).getValue();
    }

    void setStonecutterSelectedRecipe(final int idx) {
        getProperty(PatternGridPropertyTypes.STONECUTTER_SELECTED_RECIPE).setValue(idx);
    }

    ItemStack getSmithingTableResult() {
        return smithingTableResult.getItem(0);
    }

    public void clear() {
        if (patternGrid != null) {
            patternGrid.clear();
        }
    }

    void sendClear() {
        C2SPackets.sendPatternGridClear();
    }

    public void createPattern() {
        if (patternGrid != null) {
            patternGrid.createPattern();
        }
    }

    void sendCreatePattern() {
        C2SPackets.sendPatternGridCreatePattern();
    }

    @API(status = API.Status.INTERNAL)
    public void transferCraftingRecipe(final List<List<ItemResource>> recipe) {
        if (patternGrid == null) {
            C2SPackets.sendPatternGridCraftingRecipeTransfer(recipe);
            return;
        }
        if (player != null) {
            patternGrid.transferCraftingRecipe(player, recipe);
        }
    }

    @API(status = API.Status.INTERNAL)
    public void transferProcessingRecipe(final List<List<ResourceAmount>> inputs,
                                         final List<List<ResourceAmount>> outputs) {
        if (patternGrid == null) {
            C2SPackets.sendPatternGridProcessingRecipeTransfer(inputs, outputs);
            return;
        }
        if (player != null) {
            patternGrid.transferProcessingRecipe(player, inputs, outputs);
        }
    }

    @API(status = API.Status.INTERNAL)
    public void transferStonecutterRecipe(final ItemResource input, final ItemResource selectedOutput) {
        if (patternGrid == null) {
            C2SPackets.sendPatternGridStonecutterRecipeTransfer(input, selectedOutput);
            return;
        }
        patternGrid.transferStonecutterRecipe(input, selectedOutput);
    }

    @API(status = API.Status.INTERNAL)
    public void transferSmithingTableRecipe(final List<ItemResource> templates,
                                            final List<ItemResource> bases,
                                            final List<ItemResource> additions) {
        if (patternGrid == null) {
            C2SPackets.sendPatternGridSmithingTableRecipeTransfer(templates, bases, additions);
            return;
        }
        if (player != null) {
            patternGrid.transferSmithingTableRecipe(player, templates, bases, additions);
        }
    }

    public void handleAllowedAlternativesUpdate(final int slotIndex, final Set<Identifier> ids) {
        processingInput.setAllowedTagIds(slotIndex, ids);
    }

    Set<Identifier> getAllowedAlternatives(final int containerSlot) {
        return processingInput.getAllowedTagIds(containerSlot);
    }

    interface PatternGridListener {
        void patternTypeChanged(PatternType newPatternType);

        void fuzzyModeChanged(boolean newFuzzyMode);
    }
}
