package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.SmithingTablePatternState;
import com.refinedmods.refinedstorage.common.autocrafting.StonecutterPatternState;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.ResourceSorters;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl.setResourceContainerData;

public class PatternGridBlockEntity extends AbstractGridBlockEntity
    implements NetworkNodeExtendedMenuProvider<PatternGridData> {
    private static final String TAG_CRAFTING_INPUT = "crafting_input";
    private static final String TAG_PATTERN_INPUT = "pattern_input";
    private static final String TAG_PATTERN_OUTPUT = "pattern_output";
    private static final String TAG_PROCESSING_INPUT = "processing_input";
    private static final String TAG_PROCESSING_INPUT_ALLOWED_TAG_IDS = "processing_input_allowed_tag_ids";
    private static final String TAG_PROCESSING_OUTPUT = "processing_output";
    private static final String TAG_FUZZY_MODE = "fuzzy_mode";
    private static final String TAG_PATTERN_TYPE = "processing";
    private static final String TAG_STONECUTTER_INPUT = "stonecutter_input";
    private static final String TAG_STONECUTTER_SELECTED_RECIPE = "stonecutter_selected_recipe";
    private static final String TAG_SMITHING_INPUT = "smithing_input";

    private final RecipeMatrix<CraftingRecipe, CraftingInput> craftingRecipe = RecipeMatrix.crafting(
        this::setChanged,
        this::getLevel
    );
    private final ProcessingMatrixInputResourceContainer processingInput = createProcessingMatrixInputContainer();
    private final ResourceContainer processingOutput = createProcessingMatrixOutputContainer();
    private final FilteredContainer patternInput = new FilteredContainer(1, PatternGridBlockEntity::isValidPattern) {
        @Override
        public void setChanged() {
            super.setChanged();
            PatternGridBlockEntity.this.setChanged();
        }
    };
    private final FilteredContainer patternOutput = new PatternOutputContainer() {
        @Override
        public void setChanged() {
            super.setChanged();
            PatternGridBlockEntity.this.setChanged();
        }
    };
    private final StonecutterInputContainer stonecutterInput = new StonecutterInputContainer(this::getLevel) {
        @Override
        public void setChanged() {
            super.setChanged();
            PatternGridBlockEntity.this.setChanged();
        }
    };
    private final RecipeMatrix<SmithingRecipe, SmithingRecipeInput> smithingTableRecipe = RecipeMatrix.smithingTable(
        this::setChanged,
        this::getLevel
    );

    private boolean fuzzyMode;
    private PatternType patternType = PatternType.CRAFTING;

    public PatternGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getPatternGrid(),
            pos,
            state,
            Platform.INSTANCE.getConfig().getPatternGrid().getEnergyUsage()
        );
        processingInput.setListener(this::setChanged);
        processingOutput.setListener(this::setChanged);
    }

    RecipeMatrixContainer getCraftingMatrix() {
        return craftingRecipe.getMatrix();
    }

    ResultContainer getCraftingResult() {
        return craftingRecipe.getResult();
    }

    ProcessingMatrixInputResourceContainer getProcessingInput() {
        return processingInput;
    }

    ResourceContainer getProcessingOutput() {
        return processingOutput;
    }

    public FilteredContainer getPatternInput() {
        return patternInput;
    }

    FilteredContainer getPatternOutput() {
        return patternOutput;
    }

    StonecutterInputContainer getStonecutterInput() {
        return stonecutterInput;
    }

    int getStonecutterSelectedRecipe() {
        return stonecutterInput.getSelectedRecipe();
    }

    void setStonecutterSelectedRecipe(final int index) {
        stonecutterInput.setSelectedRecipe(index);
        setChanged();
    }

    RecipeMatrixContainer getSmithingTableMatrix() {
        return smithingTableRecipe.getMatrix();
    }

    ResultContainer getSmithingTableResult() {
        return smithingTableRecipe.getResult();
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_PATTERN_INPUT, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(patternInput.getItems()));
        output.store(TAG_PATTERN_OUTPUT, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(patternOutput.getItems()));
        output.putBoolean(TAG_FUZZY_MODE, fuzzyMode);
        output.putInt(TAG_PATTERN_TYPE, PatternTypeSettings.getPatternType(patternType));
        output.store(TAG_PROCESSING_INPUT, ResourceCodecs.CONTAINER_CONTENTS_CODEC,
            ResourceContainerContents.of(processingInput));
        output.store(TAG_PROCESSING_INPUT_ALLOWED_TAG_IDS, ProcessingMatrixInputResourceContainer.ALLOWED_TAG_IDS_CODEC,
            processingInput.getAllowedTagIds());
        output.store(TAG_PROCESSING_OUTPUT, ResourceCodecs.CONTAINER_CONTENTS_CODEC,
            ResourceContainerContents.of(processingOutput));
        output.store(TAG_STONECUTTER_INPUT, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(stonecutterInput.getItems()));
        output.putInt(TAG_STONECUTTER_SELECTED_RECIPE, stonecutterInput.getSelectedRecipe());
        output.store(TAG_SMITHING_INPUT, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(smithingTableRecipe.getMatrix().getItems()));
        output.store(TAG_CRAFTING_INPUT, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(craftingRecipe.getMatrix().getItems()));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_PATTERN_INPUT, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(patternInput.getItems()));
        input.read(TAG_PATTERN_OUTPUT, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(patternOutput.getItems()));
        fuzzyMode = input.getBooleanOr(TAG_FUZZY_MODE, false);
        patternType = input.getInt(TAG_PATTERN_TYPE)
            .map(PatternTypeSettings::getPatternType)
            .orElse(PatternType.CRAFTING);
        input.read(TAG_PROCESSING_INPUT, ResourceCodecs.CONTAINER_CONTENTS_CODEC)
            .ifPresent(processingInput::load);
        input.read(TAG_PROCESSING_INPUT_ALLOWED_TAG_IDS, ProcessingMatrixInputResourceContainer.ALLOWED_TAG_IDS_CODEC)
            .ifPresent(allowedTagIds -> {
                for (int i = 0; i < allowedTagIds.size(); ++i) {
                    final Set<Identifier> ids = allowedTagIds.get(i);
                    processingInput.setAllowedTagIdsSilently(i, ids);
                }
            });
        input.read(TAG_PROCESSING_OUTPUT, ResourceCodecs.CONTAINER_CONTENTS_CODEC)
            .ifPresent(processingOutput::load);
        input.read(TAG_STONECUTTER_INPUT, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(stonecutterInput.getItems()));
        input.getInt(TAG_STONECUTTER_SELECTED_RECIPE).ifPresent(stonecutterInput::setSelectedRecipe);
        input.read(TAG_SMITHING_INPUT, ItemContainerContents.CODEC).ifPresent(smithingTableRecipe::load);
        input.read(TAG_CRAFTING_INPUT, ItemContainerContents.CODEC).ifPresent(craftingRecipe::load);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        craftingRecipe.updateResult(level);
        stonecutterInput.updateRecipes(level);
        smithingTableRecipe.updateResult(level);
    }

    boolean isFuzzyMode() {
        return fuzzyMode;
    }

    PatternType getPatternType() {
        return patternType;
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        setChanged();
    }

    void setPatternType(final PatternType patternType) {
        this.patternType = patternType;
        setChanged();
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.PATTERN_GRID);
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PatternGridContainerMenu(syncId, inventory, this);
    }

    @Override
    public PatternGridData getMenuData() {
        return new PatternGridData(
            GridData.of(this),
            patternType,
            ProcessingInputData.of(processingInput),
            ResourceContainerData.of(processingOutput),
            stonecutterInput.getSelectedRecipe()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PatternGridData> getMenuCodec() {
        return PatternGridData.STREAM_CODEC;
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            final NonNullList<ItemStack> drops = NonNullList.create();
            drops.add(patternInput.getItem(0));
            drops.add(patternOutput.getItem(0));
            Containers.dropContents(level, pos, drops);
        }
    }

    void clear() {
        if (level == null) {
            return;
        }
        switch (patternType) {
            case CRAFTING -> craftingRecipe.clear(level);
            case PROCESSING -> clearProcessing();
            case STONECUTTER -> stonecutterInput.clearContent();
            case SMITHING_TABLE -> smithingTableRecipe.clear(level);
        }
        setChanged();
    }

    private void clearProcessing() {
        processingInput.clear();
        processingOutput.clear();
    }

    void createPattern() {
        if (level == null || !isPatternAvailable()) {
            return;
        }
        final ItemStack result = switch (patternType) {
            case CRAFTING -> createCraftingPattern();
            case PROCESSING -> createProcessingPattern();
            case STONECUTTER -> createStonecutterPattern();
            case SMITHING_TABLE -> createSmithingTablePattern();
        };
        if (result != null) {
            final boolean shouldExtractInputPattern = patternOutput.getItem(0).isEmpty();
            if (shouldExtractInputPattern) {
                patternInput.removeItem(0, 1);
            }
            patternOutput.setItem(0, result);
        }
    }

    @Nullable
    private ItemStack createCraftingPattern() {
        if (!craftingRecipe.hasResult()) {
            return null;
        }
        final ItemStack result = createPatternStack(PatternType.CRAFTING);
        final CraftingPatternState state = new CraftingPatternState(
            fuzzyMode,
            getCraftingMatrix().asPositionedCraftInput()
        );
        result.set(DataComponents.INSTANCE.getCraftingPatternState(), state);
        return result;
    }

    @Nullable
    private ItemStack createProcessingPattern() {
        if (processingInput.isEmpty() || processingOutput.isEmpty()) {
            return null;
        }
        final ItemStack result = createPatternStack(PatternType.PROCESSING);
        final List<Optional<ProcessingPatternState.ProcessingIngredient>> ingredients = new ArrayList<>();
        for (int i = 0; i < processingInput.size(); ++i) {
            ingredients.add(processingInput.getIngredient(i));
        }
        final List<Optional<ResourceAmount>> outputs = new ArrayList<>();
        for (int i = 0; i < processingOutput.size(); ++i) {
            outputs.add(Optional.ofNullable(processingOutput.get(i)));
        }
        final ProcessingPatternState patternProcessingState = new ProcessingPatternState(
            ingredients,
            outputs
        );
        result.set(DataComponents.INSTANCE.getProcessingPatternState(), patternProcessingState);
        return result;
    }

    @Nullable
    private ItemStack createStonecutterPattern() {
        if (stonecutterInput.getItem(0).isEmpty() || !stonecutterInput.hasSelectedRecipe() || level == null) {
            return null;
        }
        final ItemStack input = stonecutterInput.getItem(0);
        final List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> recipes =
            stonecutterInput.getRecipes().entries();
        final int selectedRecipe = stonecutterInput.getSelectedRecipe();
        if (selectedRecipe < 0 || selectedRecipe >= recipes.size()) {
            return null;
        }
        return recipes.get(selectedRecipe).recipe().recipe().map(holder -> {
            final ItemStack selectedOutput = holder.value().assemble(new SingleRecipeInput(input));
            if (selectedOutput.isEmpty()) {
                return null;
            }
            final ItemStack result = createPatternStack(PatternType.STONECUTTER);
            final StonecutterPatternState state = new StonecutterPatternState(
                ItemResource.ofItemStack(input),
                ItemResource.ofItemStack(selectedOutput)
            );
            result.set(DataComponents.INSTANCE.getStonecutterPatternState(), state);
            return result;
        }).orElse(null);
    }

    @Nullable
    private ItemStack createSmithingTablePattern() {
        if (!smithingTableRecipe.hasResult()) {
            return null;
        }
        final ItemStack result = createPatternStack(PatternType.SMITHING_TABLE);
        final SmithingTablePatternState state = new SmithingTablePatternState(
            ItemResource.ofItemStack(smithingTableRecipe.getMatrix().getItem(0)),
            ItemResource.ofItemStack(smithingTableRecipe.getMatrix().getItem(1)),
            ItemResource.ofItemStack(smithingTableRecipe.getMatrix().getItem(2))
        );
        result.set(DataComponents.INSTANCE.getSmithingTablePatternState(), state);
        return result;
    }

    public static ItemStack createPatternStack(final PatternType patternType) {
        final ItemStack result = new ItemStack(Items.INSTANCE.getPattern());
        final PatternState patternState = new PatternState(UUID.randomUUID(), patternType);
        result.set(DataComponents.INSTANCE.getPatternState(), patternState);
        return result;
    }

    void copyPattern(final ItemStack stack) {
        final PatternState patternState = stack.get(DataComponents.INSTANCE.getPatternState());
        if (patternState == null) {
            return;
        }
        this.patternType = patternState.type();
        switch (patternState.type()) {
            case CRAFTING -> {
                final CraftingPatternState state = stack.get(DataComponents.INSTANCE.getCraftingPatternState());
                if (state != null) {
                    copyCraftingPattern(state);
                }
            }
            case PROCESSING -> {
                final ProcessingPatternState state = stack.get(DataComponents.INSTANCE.getProcessingPatternState());
                if (state != null) {
                    copyProcessingPattern(state);
                }
            }
            case STONECUTTER -> {
                final StonecutterPatternState state = stack.get(DataComponents.INSTANCE.getStonecutterPatternState());
                if (state != null) {
                    copyStonecutterPattern(state);
                }
            }
            case SMITHING_TABLE -> {
                final SmithingTablePatternState state = stack.get(
                    DataComponents.INSTANCE.getSmithingTablePatternState()
                );
                if (state != null) {
                    copySmithingTablePattern(state);
                }
            }
        }
        setChanged();
    }

    private void copyCraftingPattern(final CraftingPatternState state) {
        this.fuzzyMode = state.fuzzyMode();
        craftingRecipe.getMatrix().clearContent();
        final CraftingInput.Positioned positionedInput = state.input();
        final int left = positionedInput.left();
        final int top = positionedInput.top();
        final CraftingInput input = positionedInput.input();
        for (int x = 0; x < input.width(); ++x) {
            for (int y = 0; y < input.height(); ++y) {
                final int matrixIndex = x + left + (y + top) * craftingRecipe.getMatrix().getWidth();
                final int recipeIndex = x + y * input.width();
                final ItemStack stack = input.getItem(recipeIndex);
                craftingRecipe.getMatrix().setItem(matrixIndex, stack);
            }
        }
        if (level != null) {
            craftingRecipe.updateResult(level);
        }
    }

    private void copyProcessingPattern(final ProcessingPatternState state) {
        clearProcessing();
        for (int i = 0; i < state.ingredients().size(); ++i) {
            final int ii = i;
            state.ingredients().get(i).ifPresent(processingIngredient -> processingInput.set(ii, processingIngredient));
        }
        for (int i = 0; i < state.outputs().size(); ++i) {
            final int ii = i;
            state.outputs().get(i).ifPresent(amount -> processingOutput.set(ii, amount));
        }
    }

    private void copyStonecutterPattern(final StonecutterPatternState state) {
        final ItemResource input = state.input();
        final ItemResource selectedOutput = state.selectedOutput();
        setStonecutterInputAndSelectedRecipe(input.toItemStack(), selectedOutput.toItemStack());
    }

    private void setStonecutterInputAndSelectedRecipe(final ItemStack input, final ItemStack selectedOutput) {
        if (level == null) {
            return;
        }
        stonecutterInput.clearContent();
        stonecutterInput.setSelectedRecipe(-1);
        stonecutterInput.setItem(0, input);
        final ContextMap context = SlotDisplayContext.fromLevel(level);
        for (int i = 0; i < stonecutterInput.getRecipes().size(); ++i) {
            final ItemStack result = stonecutterInput.getRecipes().entries().get(i).recipe().optionDisplay()
                .resolveForFirstStack(context);
            if (ItemStack.isSameItemSameComponents(result, selectedOutput)) {
                stonecutterInput.setSelectedRecipe(i);
                return;
            }
        }
    }

    private void copySmithingTablePattern(final SmithingTablePatternState state) {
        final ItemResource template = state.template();
        final ItemResource base = state.base();
        final ItemResource addition = state.addition();
        setSmithingTableInput(template.toItemStack(), base.toItemStack(), addition.toItemStack());
    }

    private void setSmithingTableInput(final ItemStack template, final ItemStack base, final ItemStack addition) {
        smithingTableRecipe.getMatrix().clearContent();
        smithingTableRecipe.getMatrix().setItem(0, template);
        smithingTableRecipe.getMatrix().setItem(1, base);
        smithingTableRecipe.getMatrix().setItem(2, addition);
        if (level != null) {
            smithingTableRecipe.updateResult(level);
        }
    }

    private boolean isPatternAvailable() {
        return !patternInput.getItem(0).isEmpty() || !patternOutput.getItem(0).isEmpty();
    }

    void transferCraftingRecipe(final Player player, final List<List<ItemResource>> recipe) {
        final Comparator<ResourceKey> sorter = ResourceSorters.create(
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(StorageNetworkComponent.class)
                : null,
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(AutocraftingNetworkComponent.class).getPatterns()
                : Collections.emptySet(),
            player.getInventory(),
            r -> r
        );
        getCraftingMatrix().clearContent();
        for (int i = 0; i < getCraftingMatrix().getContainerSize(); ++i) {
            if (i >= recipe.size()) {
                break;
            }
            final List<ItemResource> possibilities = recipe.get(i);
            if (!possibilities.isEmpty()) {
                possibilities.sort(sorter);
                getCraftingMatrix().setItem(i, possibilities.getFirst().toItemStack());
            }
        }
        setPatternType(PatternType.CRAFTING);
    }

    void transferProcessingRecipe(final Player player,
                                  final List<List<ResourceAmount>> inputs,
                                  final List<List<ResourceAmount>> outputs) {
        final Comparator<ResourceAmount> sorter = ResourceSorters.create(
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(StorageNetworkComponent.class)
                : null,
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(AutocraftingNetworkComponent.class).getPatterns()
                : Collections.emptySet(),
            player.getInventory(),
            ResourceAmount::resource
        );
        getProcessingInput().clear();
        transferProcessingRecipe(inputs, getProcessingInput(), sorter);
        getProcessingOutput().clear();
        transferProcessingRecipe(outputs, getProcessingOutput(), sorter);
        setPatternType(PatternType.PROCESSING);
    }

    private void transferProcessingRecipe(final List<List<ResourceAmount>> recipe,
                                          final ResourceContainer container,
                                          final Comparator<ResourceAmount> sorter) {
        for (int i = 0; i < container.size(); ++i) {
            if (i >= recipe.size()) {
                break;
            }
            final List<ResourceAmount> possibilities = recipe.get(i);
            if (!possibilities.isEmpty()) {
                possibilities.sort(sorter);
                container.set(i, possibilities.getFirst());
            }
        }
    }

    void transferStonecutterRecipe(final ItemResource input, final ItemResource selectedOutput) {
        setStonecutterInputAndSelectedRecipe(input.toItemStack(), selectedOutput.toItemStack());
        setPatternType(PatternType.STONECUTTER);
    }

    void transferSmithingTableRecipe(final Player player,
                                     final List<ItemResource> templates,
                                     final List<ItemResource> bases,
                                     final List<ItemResource> additions) {
        if (templates.isEmpty() || bases.isEmpty() || additions.isEmpty()) {
            return;
        }
        final Comparator<ItemResource> sorter = ResourceSorters.create(
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(StorageNetworkComponent.class)
                : null,
            mainNetworkNode.getNetwork() != null
                ? mainNetworkNode.getNetwork().getComponent(AutocraftingNetworkComponent.class).getPatterns()
                : Collections.emptySet(),
            player.getInventory(),
            r -> r
        );
        templates.sort(sorter);
        bases.sort(sorter);
        additions.sort(sorter);
        final ItemResource template = templates.getFirst();
        final ItemResource base = bases.getFirst();
        final ItemResource addition = additions.getFirst();
        setSmithingTableInput(template.toItemStack(), base.toItemStack(), addition.toItemStack());
        setPatternType(PatternType.SMITHING_TABLE);
    }

    static boolean isValidPattern(final ItemStack stack) {
        return stack.getItem() instanceof PatternItem;
    }

    static ProcessingMatrixInputResourceContainer createProcessingMatrixInputContainer() {
        return new ProcessingMatrixInputResourceContainer(
            81,
            PatternGridBlockEntity::getProcessingPatternLimit,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ProcessingMatrixInputResourceContainer createProcessingMatrixInputContainer(final ProcessingInputData data) {
        final ProcessingMatrixInputResourceContainer filterContainer = createProcessingMatrixInputContainer();
        setResourceContainerData(data.resourceContainerData().resources(), filterContainer);
        for (int i = 0; i < data.allowedTagIds().size(); ++i) {
            filterContainer.setAllowedTagIds(i, data.allowedTagIds().get(i));
        }
        return filterContainer;
    }

    static ResourceContainer createProcessingMatrixOutputContainer() {
        return new ResourceContainerImpl(
            81,
            PatternGridBlockEntity::getProcessingPatternLimit,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ResourceContainer createProcessingMatrixOutputContainer(final ResourceContainerData data) {
        final ResourceContainer filterContainer = createProcessingMatrixOutputContainer();
        setResourceContainerData(data.resources(), filterContainer);
        return filterContainer;
    }

    private static long getProcessingPatternLimit(final ResourceKey resource) {
        return resource instanceof PlatformResourceKey platformResource
            ? platformResource.getProcessingPatternLimit()
            : 1;
    }
}
