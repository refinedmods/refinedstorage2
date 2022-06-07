package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.PlatformTestFixtures;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;

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
    Listener listener;
    ResourceFilterContainer sut;

    @BeforeEach
    void setUp() {
        listener = new Listener();
        sut = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
    }

    @Test
    void Test_initial_state() {
        // Assert
        assertThat(listener.changed).isFalse();
        assertThat(sut.getType(0)).isNull();
        assertThat(sut.getType(1)).isNull();
        assertThat(sut.getType(2)).isNull();
        assertThat(sut.getFilter(0)).isNull();
        assertThat(sut.getFilter(1)).isNull();
        assertThat(sut.getFilter(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).isEmpty();
        assertThat(sut.determineDefaultType()).isEqualTo(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY.getDefault());
    }

    @Test
    void Test_setting_filter() {
        // Arrange
        ItemResource value = new ItemResource(Items.DIRT, null);

        // Act
        sut.set(1, ItemResourceType.INSTANCE, value);

        // Assert
        assertThat(listener.changed).isTrue();
        assertThat(sut.getType(0)).isNull();
        assertThat(sut.getType(1)).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(sut.getType(2)).isNull();
        assertThat(sut.getFilter(0)).isNull();
        assertThat(sut.getFilter(1)).isEqualTo(value);
        assertThat(sut.getFilter(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).containsExactly(value);
        assertThat(sut.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_removing_filter() {
        // Arrange
        sut.set(1, ItemResourceType.INSTANCE, new ItemResource(Items.DIRT, null));
        listener.reset();

        // Act
        sut.remove(1);

        // Assert
        assertThat(listener.changed).isTrue();
        assertThat(sut.getType(0)).isNull();
        assertThat(sut.getType(1)).isNull();
        assertThat(sut.getType(2)).isNull();
        assertThat(sut.getFilter(0)).isNull();
        assertThat(sut.getFilter(1)).isNull();
        assertThat(sut.getFilter(2)).isNull();
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getTemplates()).isEmpty();
        assertThat(sut.determineDefaultType()).isEqualTo(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY.getDefault());
    }

    @Test
    void Test_serializing_and_deserializing() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, ItemResourceType.INSTANCE, itemValue);
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, FluidResourceType.INSTANCE, fluidValue);
        listener.reset();

        // Act
        CompoundTag serialized = sut.toTag();
        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.load(serialized);

        // Assert
        assertThat(listener.changed).isFalse();
        assertThat(deserialized.getType(0)).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(deserialized.getType(1)).isNull();
        assertThat(deserialized.getType(2)).isEqualTo(FluidResourceType.INSTANCE);
        assertThat(deserialized.getFilter(0)).isEqualTo(itemValue);
        assertThat(deserialized.getFilter(1)).isNull();
        assertThat(deserialized.getFilter(2)).isEqualTo(fluidValue);
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue, fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_serializing_and_deserializing_with_invalid_type() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, ItemResourceType.INSTANCE, itemValue);
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, FluidResourceType.INSTANCE, fluidValue);
        listener.reset();

        // Act
        CompoundTag serialized = sut.toTag();
        serialized.getCompound("s0").putString("t", "invalid");
        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.load(serialized);

        // Assert
        assertThat(listener.changed).isFalse();
        assertThat(deserialized.getType(0)).isNull();
        assertThat(deserialized.getType(1)).isNull();
        assertThat(deserialized.getType(2)).isEqualTo(FluidResourceType.INSTANCE);
        assertThat(deserialized.getFilter(0)).isNull();
        assertThat(deserialized.getFilter(1)).isNull();
        assertThat(deserialized.getFilter(2)).isEqualTo(fluidValue);
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(FluidResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_unique_item() {
        // Arrange
        sut.set(0, ItemResourceType.INSTANCE, new ItemResource(Items.DIRT, null));

        // Act
        ResourceType<?> defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_unique_fluid() {
        // Arrange
        sut.set(0, FluidResourceType.INSTANCE, new FluidResource(Fluids.LAVA, null));

        // Act
        ResourceType<?> defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(FluidResourceType.INSTANCE);
    }

    @Test
    void Test_determining_default_type_when_mixed_resources() {
        // Arrange
        sut.set(0, FluidResourceType.INSTANCE, new FluidResource(Fluids.LAVA, null));
        sut.set(1, ItemResourceType.INSTANCE, new ItemResource(Items.DIRT, null));

        // Act
        ResourceType<?> defaultType = sut.determineDefaultType();

        // Assert
        assertThat(defaultType).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_network_serializing_and_deserializing() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, ItemResourceType.INSTANCE, itemValue);
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, FluidResourceType.INSTANCE, fluidValue);
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
        assertThat(listener.changed).isFalse();
        assertThat(deserialized.getType(0)).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(deserialized.getType(1)).isNull();
        assertThat(deserialized.getType(2)).isEqualTo(FluidResourceType.INSTANCE);
        assertThat(deserialized.getFilter(0)).isEqualTo(itemValue);
        assertThat(deserialized.getFilter(1)).isNull();
        assertThat(deserialized.getFilter(2)).isEqualTo(fluidValue);
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue, fluidValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_network_serializing_and_deserializing_with_invalid_type() {
        // Arrange
        ItemResource itemValue = new ItemResource(Items.DIRT, null);
        sut.set(0, ItemResourceType.INSTANCE, itemValue);
        FluidResource fluidValue = new FluidResource(Fluids.LAVA, null);
        sut.set(2, FluidResourceType.INSTANCE, fluidValue);
        listener.reset();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        // Act
        sut.writeToUpdatePacket(0, buf);
        sut.writeToUpdatePacket(1, buf);

        buf.writeBoolean(true);
        buf.writeUtf("invalid");
        FluidResourceType.INSTANCE.writeToPacket(buf, fluidValue);

        ResourceFilterContainer deserialized = new ResourceFilterContainer(PlatformTestFixtures.RESOURCE_TYPE_REGISTRY, 3, listener);
        deserialized.readFromUpdatePacket(0, buf);
        deserialized.readFromUpdatePacket(1, buf);
        deserialized.readFromUpdatePacket(2, buf);

        // Assert
        assertThat(listener.changed).isFalse();
        assertThat(deserialized.getType(0)).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(deserialized.getType(1)).isNull();
        assertThat(deserialized.getType(2)).isNull();
        assertThat(deserialized.getFilter(0)).isEqualTo(itemValue);
        assertThat(deserialized.getFilter(1)).isNull();
        assertThat(deserialized.getFilter(2)).isNull();
        assertThat(deserialized.size()).isEqualTo(3);
        assertThat(deserialized.getTemplates()).containsExactlyInAnyOrder(itemValue);
        assertThat(deserialized.determineDefaultType()).isEqualTo(ItemResourceType.INSTANCE);
    }

    private static class Listener implements Runnable {
        private boolean changed;

        @Override
        public void run() {
            this.changed = true;
        }

        public void reset() {
            this.changed = false;
        }
    }
}
