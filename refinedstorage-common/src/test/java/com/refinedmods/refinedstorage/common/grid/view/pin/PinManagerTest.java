package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.MinecraftRegistriesTest;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceType;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@MinecraftRegistriesTest
class PinManagerTest {
    private static final ResourceRepositoryMapper<GridResource> MAPPER = resource ->
        new TestGridResource((PlatformResourceKey) resource);

    private InMemoryPinRepository repository;
    private PinManager sut;

    private ItemResource dirt;
    private ItemResource stone;
    private ItemResource gold;

    private GridResource dirtResource;
    private GridResource stoneResource;
    private GridResource goldResource;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPinRepository();
        sut = new PinManager(repository, MAPPER);
        dirt = new ItemResource(Items.DIRT);
        stone = new ItemResource(Items.STONE);
        gold = new ItemResource(Items.GOLD_INGOT);
        dirtResource = MAPPER.apply(dirt);
        stoneResource = MAPPER.apply(stone);
        goldResource = MAPPER.apply(gold);
    }

    @Test
    void shouldLoadPinsFromRepositoryOnConstruction() {
        // Arrange
        repository.pins.add(manualPin(dirtResource));
        repository.pins.add(manualPin(stoneResource));

        // Act
        final PinManager pinManager = new PinManager(repository, MAPPER);

        // Assert
        assertThat(pinManager.getAll()).containsExactly(
            manualPin(dirtResource),
            manualPin(stoneResource)
        );
    }

    @Test
    void shouldAddManualPin() {
        // Act
        sut.add(0, dirtResource);

        // Assert
        assertThat(sut.getAll()).containsExactly(manualPin(dirtResource));
        assertThat(repository.lastSaved).containsExactly(manualPin(dirtResource));
    }

    @Test
    void shouldRespectInsertionIndexWhenAddingPin() {
        // Act
        sut.add(0, dirtResource);
        sut.add(0, stoneResource);
        sut.add(1, goldResource);

        // Assert
        assertThat(sut.getAll()).containsExactly(
            manualPin(stoneResource),
            manualPin(goldResource),
            manualPin(dirtResource)
        );
    }

    @Test
    void shouldNotAddDuplicateManualPin() {
        // Arrange
        sut.add(0, dirtResource);
        repository.lastSaved = null;

        // Act
        sut.add(0, dirtResource);

        // Assert
        assertThat(sut.getAll()).containsExactly(manualPin(dirtResource));
        assertThat(repository.lastSaved).isNull();
    }

    @Test
    void shouldRemovePin() {
        // Arrange
        sut.add(0, dirtResource);
        sut.add(1, stoneResource);

        // Act
        final Pin removed = sut.remove(0);

        // Assert
        assertThat(removed).isEqualTo(manualPin(dirtResource));
        assertThat(sut.getAll()).containsExactly(manualPin(stoneResource));
        assertThat(repository.lastSaved).containsExactly(manualPin(stoneResource));
    }

    @Test
    void shouldReportContains() {
        // Arrange
        sut.add(0, dirtResource);

        // Act & Assert
        assertThat(sut.contains(dirtResource)).isTrue();
        assertThat(sut.contains(stoneResource)).isFalse();
    }

    @Test
    void shouldAddAutocraftingPinForNewResource() {
        // Arrange
        final TaskId task = TaskId.create();

        // Act
        sut.loadAutocrafting(Map.of(dirt, Set.of(task)));

        // Assert
        assertThat(sut.getAll()).containsExactly(autocraftingPin(dirtResource));
        assertThat(sut.getAutocraftingTasks(dirt)).containsExactly(task);
    }

    @Test
    void shouldNotAddAutocraftingPinIfManualPinAlreadyExists() {
        // Arrange
        sut.add(0, dirtResource);

        // Act
        sut.loadAutocrafting(Map.of(dirt, Set.of(TaskId.create())));

        // Assert
        assertThat(sut.getAll()).containsExactly(manualPin(dirtResource));
    }

    @Test
    void shouldNotDuplicateAutocraftingPinAcrossLoads() {
        // Arrange
        final TaskId firstTask = TaskId.create();
        final TaskId secondTask = TaskId.create();
        sut.loadAutocrafting(Map.of(dirt, Set.of(firstTask)));

        // Act
        sut.loadAutocrafting(Map.of(dirt, Set.of(secondTask)));

        // Assert
        assertThat(sut.getAll()).containsExactly(autocraftingPin(dirtResource));
        assertThat(sut.getAutocraftingTasks(dirt)).containsExactly(secondTask);
    }

    @Test
    void shouldPurgeAutocraftingPinOnceResourceIsNoLongerActiveAndLoadIsCalledAgain() {
        // Arrange
        sut.loadAutocrafting(Map.of(dirt, Set.of(TaskId.create())));

        // Act & Assert: first load with empty marks the resource for purge but keeps the pin
        sut.loadAutocrafting(Map.of());
        assertThat(sut.getAll()).containsExactly(autocraftingPin(dirtResource));
        assertThat(sut.getAutocraftingTasks(dirt)).isEmpty();

        // Act & Assert: a subsequent load actually purges
        sut.loadAutocrafting(Map.of());
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldNotPurgeManualPinsWhenLoadingAutocrafting() {
        // Arrange
        sut.add(0, dirtResource);

        // Act
        sut.loadAutocrafting(Map.of(dirt, Set.of(TaskId.create())));
        sut.loadAutocrafting(Map.of());
        sut.loadAutocrafting(Map.of());

        // Assert
        assertThat(sut.getAll()).containsExactly(manualPin(dirtResource));
    }

    @Test
    void shouldOnlyPurgeAutocraftingPinsForResourcesThatAreNoLongerActive() {
        // Arrange
        sut.loadAutocrafting(Map.of(dirt, Set.of(TaskId.create()), stone, Set.of(TaskId.create())));

        // Act
        sut.loadAutocrafting(Map.of(stone, Set.of(TaskId.create())));
        sut.loadAutocrafting(Map.of(stone, Set.of(TaskId.create())));

        // Assert
        assertThat(sut.getAll()).containsExactly(autocraftingPin(stoneResource));
    }

    @Test
    void shouldReturnEmptyTasksForUnknownResource() {
        // Act & Assert
        assertThat(sut.getAutocraftingTasks(gold)).isEmpty();
    }

    private static Pin manualPin(final GridResource gridResource) {
        return new Pin(gridResource, true);
    }

    private static Pin autocraftingPin(final GridResource gridResource) {
        return new Pin(gridResource, false);
    }

    private static class InMemoryPinRepository implements PinRepository {
        private final List<Pin> pins = new ArrayList<>();
        @Nullable
        private List<Pin> lastSaved;

        @Override
        public void saveAll(final List<Pin> pinsToSave) {
            lastSaved = List.copyOf(pinsToSave);
            pins.clear();
            pins.addAll(pinsToSave);
        }

        @Override
        public List<Pin> loadAll() {
            return new ArrayList<>(pins);
        }
    }

    private record TestGridResource(PlatformResourceKey resource) implements GridResource {
        @Override
        public boolean is(final GridResource other) {
            return other instanceof TestGridResource(PlatformResourceKey otherResource)
                && resource.equals(otherResource);
        }

        @Override
        public PlatformResourceKey getAutocraftingResource() {
            return resource;
        }

        @Override
        @Nullable
        public TrackedResource getTrackedResource(
            final Function<ResourceKey, @Nullable TrackedResource> trackedResourceProvider
        ) {
            return null;
        }

        @Override
        public long getAmount(final ResourceRepository<GridResource> repository) {
            return 0;
        }

        @Override
        public String getSortName() {
            return "";
        }

        @Override
        public Set<String> getSearchableNames() {
            return Set.of();
        }

        @Override
        public Set<String> getAttribute(final GridResourceAttributeKey key) {
            return Set.of();
        }

        @Override
        public boolean isAutocraftable(final ResourceRepository<GridResource> repository) {
            return false;
        }

        @Override
        public boolean canExtract(final ItemStack carriedStack, final ResourceRepository<GridResource> repository) {
            return false;
        }

        @Override
        public void onExtract(final GridExtractMode extractMode,
                              final boolean cursor,
                              final GridExtractionStrategy extractionStrategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void render(final GuiGraphicsExtractor graphics, final int x, final int y) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDisplayedAmount(final ResourceRepository<GridResource> repository) {
            return "";
        }

        @Override
        public String getAmountInTooltip(final ResourceRepository<GridResource> repository) {
            return "";
        }

        @Override
        public List<Component> getTooltip() {
            return List.of();
        }

        @Override
        public Optional<TooltipComponent> getTooltipImage() {
            return Optional.empty();
        }

        @Override
        public int getRegistryId() {
            return 0;
        }

        @Override
        public List<ClientTooltipComponent> getExtractionHints(final ItemStack carriedStack,
                                                               final ResourceRepository<GridResource> repository) {
            return List.of();
        }

        @Override
        public GridResourceType getType() {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public ResourceAmount createAutocraftingRequest() {
            throw new UnsupportedOperationException();
        }
    }
}
