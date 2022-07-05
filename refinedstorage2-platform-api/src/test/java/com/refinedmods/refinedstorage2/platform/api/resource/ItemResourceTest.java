package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetupMinecraft
class ItemResourceTest {
    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidItem() {
        // Assert
        assertThrows(NullPointerException.class, () -> new ItemResource(null, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSerialization(final boolean hasTag) {
        // Arrange
        final CompoundTag itemTag = hasTag ? createDummyTag() : null;
        final ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        final CompoundTag serialized = ItemResource.toTag(itemResource);
        final Optional<ItemResource> deserialized = ItemResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isPresent().contains(itemResource);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSerializationWithAmount(final boolean hasTag) {
        // Arrange
        final CompoundTag itemTag = hasTag ? createDummyTag() : null;
        final ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);
        final ResourceAmount<ItemResource> resourceAmount = new ResourceAmount<>(itemResource, 10);

        // Act
        final CompoundTag serialized = ItemResource.toTagWithAmount(resourceAmount);
        final Optional<ResourceAmount<ItemResource>> deserialized = ItemResource.fromTagWithAmount(serialized);

        // Assert
        assertThat(deserialized).isPresent();
        assertThat(deserialized.get()).usingRecursiveComparison().isEqualTo(resourceAmount);
    }

    @Test
    void testDeserializationWithInvalidItem() {
        // Arrange
        final ItemResource itemResource = new ItemResource(Items.DIRT, null);
        final CompoundTag serialized = ItemResource.toTag(itemResource);
        serialized.putString("id", "minecraft:non_existent");

        // Act
        final Optional<ItemResource> deserialized = ItemResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testToPlatformStack(final boolean hasTag) {
        // Arrange
        final CompoundTag itemTag = hasTag ? createDummyTag() : null;
        final ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        final ItemStack stack = itemResource.toItemStack();

        // Assert
        assertThat(stack.getItem()).isEqualTo(Items.DIRT);
        assertThat(stack.getTag()).isEqualTo(itemTag);
        assertThat(stack.getCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testNormalization(final boolean hasTag) {
        // Arrange
        final CompoundTag itemTag = hasTag ? createDummyTag() : null;
        final ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        final ItemResource normalized = itemResource.normalize();

        // Assert
        assertThat(normalized.item()).isEqualTo(Items.DIRT);
        assertThat(normalized.tag()).isNull();
    }

    @Test
    void testEqualsHashcode() {
        // Assert
        EqualsVerifier.forClass(ItemResource.class)
            .withPrefabValues(Item.class, Items.DIRT, Items.GLASS)
            .withNonnullFields("item")
            .verify();
    }
}
