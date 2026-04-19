package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilder;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilderImpl;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.MinecraftRegistriesTest;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.view.AbstractItemGridResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@MinecraftRegistriesTest
class GridSortingTypesTest {
    private static final ResourceRepositoryMapper<GridResource> MAPPER =
        new AbstractItemGridResourceRepositoryMapper() {
            @Override
            public String getModId(final ItemStack itemStack) {
                return "";
            }

            @Override
            public Optional<String> getModName(final String modId) {
                return Optional.empty();
            }
        };

    private ResourceRepositoryBuilder<GridResource> builder;
    private ItemResource dirt;
    private ItemResource stone;
    private ItemResource gold;

    @BeforeEach
    void setUp() {
        builder = new ResourceRepositoryBuilderImpl<>(
            MAPPER,
            view -> Comparator.comparing(GridResource::getHoverName),
            view -> Comparator.comparingLong(resource -> resource.getAmount(view))
        );
        dirt = new ItemResource(Items.DIRT, DataComponentPatch.EMPTY);
        stone = new ItemResource(Items.STONE, DataComponentPatch.EMPTY);
        gold = new ItemResource(Items.GOLD_INGOT, DataComponentPatch.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(GridSortingTypes.class)
    void testSortingAscending(final GridSortingTypes sortingType) {
        // Arrange
        final ResourceRepository<GridResource> repository = builder
            .addResource(dirt, 10)
            .addResource(dirt, 5)
            .addResource(stone, 1)
            .addResource(gold, 2)
            .build();

        final Function<ResourceKey, @Nullable TrackedResource> trackedResourceProvider =
            resource -> resource == dirt ? new TrackedResource("Raoul", 3)
                : (resource == stone ? new TrackedResource("Raoul2", 2) : null);

        repository.setSort(sortingType
                .apply(gridResource ->
                    gridResource.getTrackedResource(trackedResourceProvider)).apply(repository),
            SortingDirection.ASCENDING);

        // Act
        repository.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Stone",
                    "Gold Ingot",
                    "Dirt"
                );
            case NAME -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Dirt",
                    "Gold Ingot",
                    "Stone"
                );
            case ID -> assertThat(repository.getViewList())
                .extracting(resource -> resource.getSearchableNames().getLast())
                .containsExactly(
                    "Stone",
                    "Dirt",
                    "Gold Ingot"
                );
            case LAST_MODIFIED -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Gold Ingot",
                    "Stone",
                    "Dirt"
                );
            default -> fail();
        }
    }

    @ParameterizedTest
    @EnumSource(GridSortingTypes.class)
    void testSortingDescending(final GridSortingTypes sortingType) {
        // Arrange
        final ResourceRepository<GridResource> repository = builder
            .addResource(dirt, 10)
            .addResource(dirt, 5)
            .addResource(stone, 1)
            .addResource(gold, 2)
            .build();

        final Function<ResourceKey, @Nullable TrackedResource> trackedResourceProvider =
            resource -> resource == dirt ? new TrackedResource("Raoul", 3)
                : (resource == stone ? new TrackedResource("Raoul2", 2) : null);

        repository.setSort(sortingType
                .apply(gridResource ->
                    gridResource.getTrackedResource(trackedResourceProvider)).apply(repository),
            SortingDirection.DESCENDING);

        // Act
        repository.sort();

        // Assert
        switch (sortingType) {
            case QUANTITY -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Dirt",
                    "Gold Ingot",
                    "Stone"
                );
            case NAME -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Stone",
                    "Gold Ingot",
                    "Dirt"
                );
            case ID -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Gold Ingot",
                    "Dirt",
                    "Stone"
                );
            case LAST_MODIFIED -> assertThat(repository.getViewList())
                .extracting(GridResource::getHoverName)
                .containsExactly(
                    "Dirt",
                    "Stone",
                    "Gold Ingot"
                );
            default -> fail();
        }
    }
}
