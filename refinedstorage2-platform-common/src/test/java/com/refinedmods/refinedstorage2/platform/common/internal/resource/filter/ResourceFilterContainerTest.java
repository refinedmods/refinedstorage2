package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.api.storage.TypedTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.SimpleListener;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemFilteredResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@SetupMinecraft
class ResourceFilterContainerTest {
    SimpleListener listener;
    ResourceFilterContainer sut;

    @BeforeEach
    void setUp() {
        listener = new SimpleListener();
        sut = new ResourceFilterContainer(3, listener, 64);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).isNull();
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).isEmpty();
        assertThat(sut.getUniqueTemplates()).isEmpty();
    }

    @Test
    void shouldSetFilter() {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);

        // Act
        sut.set(1, new ItemFilteredResource(value, 1));

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 64})
    void shouldChangeAmount(final long newAmount) {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);
        sut.set(1, new ItemFilteredResource(value, 2));

        // Act
        sut.setAmount(1, newAmount);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, newAmount));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void shouldNotChangeAmountIfTooSmall(final long newAmount) {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);
        sut.set(1, new ItemFilteredResource(value, 3));

        // Act
        sut.setAmount(1, newAmount);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @ParameterizedTest
    @ValueSource(longs = {64, 65})
    void shouldNotChangeAmountIfTooLargeRespectingMaxAmountInContainer(final long newAmount) {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);
        sut.set(1, new ItemFilteredResource(value, 3));

        // Act
        sut.setAmount(1, newAmount);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 64));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @ParameterizedTest
    @ValueSource(longs = {16, 17})
    void shouldNotChangeAmountIfTooLargeRespectingMaxAmountInFilteredResource(final long newAmount) {
        // Arrange
        final ItemResource value = new ItemResource(Items.BUCKET, null);
        sut.set(1, new ItemFilteredResource(value, 3));

        // Act
        sut.setAmount(1, newAmount);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 16));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @Test
    void shouldNotChangeAmountIfResourceIsNotPresent() {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);
        sut.set(1, new ItemFilteredResource(value, 1));

        // Act
        sut.setAmount(2, 10);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @Test
    void shouldNotChangeAmountIfAmountIsUnsupported() {
        // Arrange
        sut = new ResourceFilterContainer(3, listener);
        final ItemResource value = new ItemResource(Items.DIRT, null);
        sut.set(1, new ItemFilteredResource(value, 1));

        // Act
        sut.setAmount(1, 10);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @Test
    void shouldSetDuplicatedFilter() {
        // Arrange
        final ItemResource value = new ItemResource(Items.DIRT, null);

        // Act
        sut.set(1, new ItemFilteredResource(value, 1));
        sut.set(2, new ItemFilteredResource(value, 1));

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.get(2)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value, 1));
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(
            new TypedTemplate<>(value, StorageChannelTypes.ITEM),
            new TypedTemplate<>(value, StorageChannelTypes.ITEM)
        );
        assertThat(sut.getUniqueTemplates()).containsExactly(new TypedTemplate<>(value, StorageChannelTypes.ITEM));
    }

    @Test
    void shouldRemoveFilter() {
        // Arrange
        sut.set(1, new ItemFilteredResource(new ItemResource(Items.DIRT, null), 1));
        listener.reset();

        // Act
        sut.remove(1);

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).isNull();
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).isEmpty();
        assertThat(sut.getUniqueTemplates()).isEmpty();
    }
}
