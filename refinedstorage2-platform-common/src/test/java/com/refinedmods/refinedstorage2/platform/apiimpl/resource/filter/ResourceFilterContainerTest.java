package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.platform.PlatformTestFixtures;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid.FluidFilteredResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.item.ItemFilteredResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;
import com.refinedmods.refinedstorage2.test.SimpleListener;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@SetupMinecraft
class ResourceFilterContainerTest {
    SimpleListener listener;
    ResourceFilterContainer sut;

    @BeforeEach
    void setUp() {
        listener = new SimpleListener();
        sut = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
    }

    @Test
    void Test_initial_state() {
        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).isNull();
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).isEmpty();
        assertThat(sut.determineDefaultType()).isEqualTo(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY.getDefault());
    }

    @Test
    void Test_setting_filter() {
        // Arrange
        ItemResource value = new ItemResource(Items.DIRT, null);

        // Act
        sut.set(1, new ItemFilteredResource(value));

        // Assert
        assertThat(listener.isChanged()).isTrue();
        assertThat(sut.get(0)).isNull();
        assertThat(sut.get(1)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(value));
        assertThat(sut.get(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(value);
        assertThat(sut.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_removing_filter() {
        // Arrange
        sut.set(1, new ItemFilteredResource(new ItemResource(Items.DIRT, null)));
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
        assertThat(sut.determineDefaultType()).isEqualTo(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY.getDefault());
    }

    @Test
    void Test_serializing_and_deserializing() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, new ItemFilteredResource(itemValue));
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, new FluidFilteredResource(fluidValue));
        listener.reset();

        // Act
        CompoundTag serialized = sut.toTag();
        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.load(serialized);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized.get(0)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(itemValue));
        assertThat(deserialized.get(1)).isNull();
        assertThat(deserialized.get(2)).usingRecursiveComparison().isEqualTo(new FluidFilteredResource(fluidValue));
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue, fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_serializing_and_deserializing_with_invalid_type() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, new ItemFilteredResource(itemValue));
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, new FluidFilteredResource(fluidValue));
        listener.reset();

        // Act
        CompoundTag serialized = sut.toTag();
        serialized.getCompound("s0").putString("t", "invalid");
        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.load(serialized);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized.get(0)).isNull();
        assertThat(deserialized.get(1)).isNull();
        assertThat(deserialized.get(2)).usingRecursiveComparison().isEqualTo(new FluidFilteredResource(fluidValue));
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(FluidResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_unique_item() {
        // Arrange
        sut.set(0, new ItemFilteredResource(new ItemResource(Items.DIRT, null)));

        // Act
        ResourceType defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_unique_fluid() {
        // Arrange
        sut.set(0, new FluidFilteredResource(new FluidResource(Fluids.LAVA, null)));

        // Act
        ResourceType defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(FluidResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_mixed_resources() {
        // Arrange
        sut.set(0, new ItemFilteredResource(new ItemResource(Items.DIRT, null)));
        sut.set(1, new FluidFilteredResource(new FluidResource(Fluids.LAVA, null)));

        // Act
        ResourceType defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_network_serializing_and_deserializing() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, new ItemFilteredResource(itemValue));
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, new FluidFilteredResource(fluidValue));
        listener.reset();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        // Act
        sut.writeToUpdatePacket(0, buf);
        sut.writeToUpdatePacket(1, buf);
        sut.writeToUpdatePacket(2, buf);

        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.readFromUpdatePacket(0, buf);
        deserialized.readFromUpdatePacket(1, buf);
        deserialized.readFromUpdatePacket(2, buf);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized.get(0)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(itemValue));
        assertThat(deserialized.get(1)).isNull();
        assertThat(deserialized.get(2)).usingRecursiveComparison().isEqualTo(new FluidFilteredResource(fluidValue));
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue, fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_network_serializing_and_deserializing_with_invalid_type() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, new ItemFilteredResource(itemValue));
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        FluidFilteredResource fluidFilteredResource = new FluidFilteredResource(fluidValue);
        sut.set(2, fluidFilteredResource);
        listener.reset();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        // Act
        sut.writeToUpdatePacket(0, buf);
        sut.writeToUpdatePacket(1, buf);

        buf.writeBoolean(true);
        buf.writeUtf("invalid");
        fluidFilteredResource.writeToPacket(buf);

        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.readFromUpdatePacket(0, buf);
        deserialized.readFromUpdatePacket(1, buf);
        deserialized.readFromUpdatePacket(2, buf);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized.get(0)).usingRecursiveComparison().isEqualTo(new ItemFilteredResource(itemValue));
        assertThat(deserialized.get(1)).isNull();
        assertThat(deserialized.get(2)).isNull();
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }
}
