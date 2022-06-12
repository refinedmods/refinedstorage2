package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class ResourceTypeRegistryImplTest {
    ResourceTypeRegistry sut;

    @BeforeEach
    void setUp() {
        sut = new ResourceTypeRegistryImpl(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_defaults() {
        // Assert
        assertThat(sut.get(ItemResourceType.INSTANCE.getId())).isNotNull();
        assertThat(sut.getDefault()).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(sut.toggle(ItemResourceType.INSTANCE)).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_registering() {
        // Act
        sut.register(FluidResourceType.INSTANCE);

        // Assert
        assertThat(sut.get(FluidResourceType.INSTANCE.getId())).isNotNull();
        assertThat(sut.getDefault()).isEqualTo(ItemResourceType.INSTANCE);
        assertThat(sut.toggle(ItemResourceType.INSTANCE)).isEqualTo(FluidResourceType.INSTANCE);
        assertThat(sut.toggle(FluidResourceType.INSTANCE)).isEqualTo(ItemResourceType.INSTANCE);
    }

    @Test
    void Test_registering_duplicate_id() {
        // Arrange
        sut.register(FluidResourceType.INSTANCE);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(new DummyResourceType(ItemResourceType.INSTANCE.getId())));
        assertThrows(IllegalArgumentException.class, () -> sut.register(new DummyResourceType(FluidResourceType.INSTANCE.getId())));
    }

    @Test
    void Test_registering_duplicate_instance() {
        // Arrange
        sut.register(FluidResourceType.INSTANCE);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(ItemResourceType.INSTANCE));
        assertThrows(IllegalArgumentException.class, () -> sut.register(FluidResourceType.INSTANCE));
    }

    @Test
    void Test_getting_non_existent_resource_type() {
        // Assert
        assertThat(sut.get(FluidResourceType.INSTANCE.getId())).isNull();
    }

    @Test
    void Test_toggling_non_existent_resource_type() {
        // Assert
        assertThat(sut.toggle(FluidResourceType.INSTANCE)).isEqualTo(ItemResourceType.INSTANCE);
    }

    private static class DummyResourceType implements ResourceType<String> {
        private final ResourceLocation id;

        public DummyResourceType(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public Component getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> translate(ItemStack stack) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void render(PoseStack poseStack, String value, int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public String readFromPacket(FriendlyByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompoundTag toTag(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> fromTag(CompoundTag tag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Component> getTooltipLines(String value, Player player) {
            throw new UnsupportedOperationException();
        }
    }
}
