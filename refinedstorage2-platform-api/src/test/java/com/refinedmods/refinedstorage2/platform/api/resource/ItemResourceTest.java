package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
@SetupMinecraft
class ItemResourceTest {
    @Test
    void Test_invalid_item() {
        // Assert
        assertThrows(NullPointerException.class, () -> new ItemResource(null, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_to_and_from_tag(boolean hasTag) {
        // Arrange
        CompoundTag itemTag = hasTag ? createDummyTag() : null;
        ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        CompoundTag serialized = ItemResource.toTag(itemResource);
        Optional<ItemResource> deserialized = ItemResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isPresent().contains(itemResource);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_to_and_from_tag_with_amount(boolean hasTag) {
        // Arrange
        CompoundTag itemTag = hasTag ? createDummyTag() : null;
        ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);
        ResourceAmount<ItemResource> resourceAmount = new ResourceAmount<>(itemResource, 10);

        // Act
        CompoundTag serialized = ItemResource.toTagWithAmount(resourceAmount);
        Optional<ResourceAmount<ItemResource>> deserialized = ItemResource.fromTagWithAmount(serialized);

        // Assert
        assertThat(deserialized).isPresent();
        assertThat(deserialized.get()).usingRecursiveComparison().isEqualTo(resourceAmount);
    }

    @Test
    void Test_from_tag_invalid_item() {
        // Arrange
        ItemResource itemResource = new ItemResource(Items.DIRT, null);
        CompoundTag serialized = ItemResource.toTag(itemResource);
        serialized.putString("id", "minecraft:non_existent");

        // Act
        Optional<ItemResource> deserialized = ItemResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_to_stack(boolean hasTag) {
        // Arrange
        CompoundTag itemTag = hasTag ? createDummyTag() : null;
        ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        ItemStack stack = itemResource.toItemStack();

        // Assert
        assertThat(stack.getItem()).isEqualTo(Items.DIRT);
        assertThat(stack.getTag()).isEqualTo(itemTag);
        assertThat(stack.getCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_normalization(boolean hasTag) {
        // Arrange
        CompoundTag itemTag = hasTag ? createDummyTag() : null;
        ItemResource itemResource = new ItemResource(Items.DIRT, itemTag);

        // Act
        ItemResource normalized = itemResource.normalize();

        // Assert
        assertThat(normalized.getItem()).isEqualTo(Items.DIRT);
        assertThat(normalized.getTag()).isNull();
    }

    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(ItemResource.class)
                .withPrefabValues(Item.class, Items.DIRT, Items.GLASS)
                .verify();
    }

    private CompoundTag createDummyTag() {
        CompoundTag itemTag = new CompoundTag();
        itemTag.putString("dummy", "test");
        return itemTag;
    }
}
