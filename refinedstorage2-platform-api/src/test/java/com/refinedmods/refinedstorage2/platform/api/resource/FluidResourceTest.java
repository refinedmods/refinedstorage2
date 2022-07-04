package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetupMinecraft
class FluidResourceTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    void testInvalidFluid() {
        // Assert
        assertThrows(NullPointerException.class, () -> new FluidResource(null, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSerialization(final boolean hasTag) {
        // Arrange
        final CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        final FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);

        // Act
        final CompoundTag serialized = FluidResource.toTag(fluidResource);
        final Optional<FluidResource> deserialized = FluidResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isPresent().contains(fluidResource);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSerializationWithAmount(final boolean hasTag) {
        // Arrange
        final CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        final FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);
        final ResourceAmount<FluidResource> resourceAmount = new ResourceAmount<>(fluidResource, 10);

        // Act
        final CompoundTag serialized = FluidResource.toTagWithAmount(resourceAmount);
        final Optional<ResourceAmount<FluidResource>> deserialized = FluidResource.fromTagWithAmount(serialized);

        // Assert
        assertThat(deserialized).isPresent();
        assertThat(deserialized.get()).usingRecursiveComparison().isEqualTo(resourceAmount);
    }

    @Test
    void testDeserializationWithInvalidFluid() {
        // Arrange
        final FluidResource fluidResource = new FluidResource(Fluids.WATER, null);
        final CompoundTag serialized = FluidResource.toTag(fluidResource);
        serialized.putString("id", "minecraft:non_existent");

        // Act
        final Optional<FluidResource> deserialized = FluidResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testNormalization(final boolean hasTag) {
        // Arrange
        final CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        final FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);

        // Act
        final FluidResource normalized = fluidResource.normalize();

        // Assert
        assertThat(normalized.fluid()).isEqualTo(Fluids.WATER);
        assertThat(normalized.tag()).isNull();
    }

    @Test
    void testEqualsHashcode() {
        // Assert
        EqualsVerifier.forClass(FluidResource.class)
            .withPrefabValues(Fluid.class, Fluids.WATER, Fluids.LAVA)
            .withNonnullFields("fluid")
            .verify();
    }

    private CompoundTag createDummyTag() {
        final CompoundTag fluidTag = new CompoundTag();
        fluidTag.putString("dummy", "test");
        return fluidTag;
    }
}
