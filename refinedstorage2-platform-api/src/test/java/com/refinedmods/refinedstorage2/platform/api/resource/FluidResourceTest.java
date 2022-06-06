package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;

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

@Rs2Test
@SetupMinecraft
class FluidResourceTest {
    @Test
    void Test_invalid_fluid() {
        // Assert
        assertThrows(NullPointerException.class, () -> new FluidResource(null, null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_to_and_from_tag(boolean hasTag) {
        // Arrange
        CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);

        // Act
        CompoundTag serialized = FluidResource.toTag(fluidResource);
        Optional<FluidResource> deserialized = FluidResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isPresent().contains(fluidResource);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_to_and_from_tag_with_amount(boolean hasTag) {
        // Arrange
        CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);
        ResourceAmount<FluidResource> resourceAmount = new ResourceAmount<>(fluidResource, 10);

        // Act
        CompoundTag serialized = FluidResource.toTagWithAmount(resourceAmount);
        Optional<ResourceAmount<FluidResource>> deserialized = FluidResource.fromTagWithAmount(serialized);

        // Assert
        assertThat(deserialized).isPresent();
        assertThat(deserialized.get()).usingRecursiveComparison().isEqualTo(resourceAmount);
    }

    @Test
    void Test_from_tag_invalid_fluid() {
        // Arrange
        FluidResource fluidResource = new FluidResource(Fluids.WATER, null);
        CompoundTag serialized = FluidResource.toTag(fluidResource);
        serialized.putString("id", "minecraft:non_existent");

        // Act
        Optional<FluidResource> deserialized = FluidResource.fromTag(serialized);

        // Assert
        assertThat(deserialized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_normalization(boolean hasTag) {
        // Arrange
        CompoundTag fluidTag = hasTag ? createDummyTag() : null;
        FluidResource fluidResource = new FluidResource(Fluids.WATER, fluidTag);

        // Act
        FluidResource normalized = fluidResource.normalize();

        // Assert
        assertThat(normalized.getFluid()).isEqualTo(Fluids.WATER);
        assertThat(normalized.getTag()).isNull();
    }

    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(FluidResource.class)
                .withPrefabValues(Fluid.class, Fluids.WATER, Fluids.LAVA)
                .verify();
    }

    private CompoundTag createDummyTag() {
        CompoundTag fluidTag = new CompoundTag();
        fluidTag.putString("dummy", "test");
        return fluidTag;
    }
}
