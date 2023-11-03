package com.refinedmods.refinedstorage2.platform.common.support.registry;

import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformRegistryImplTest {
    private static final ResourceLocation A = new ResourceLocation("minecraft:a");
    private static final ResourceLocation B = new ResourceLocation("minecraft:b");
    private static final ResourceLocation C = new ResourceLocation("minecraft:c");

    PlatformRegistry<Integer> sut;

    @BeforeEach
    void setUp() {
        sut = new PlatformRegistryImpl<>(A, 10);
    }

    @Test
    void testDefaults() {
        // Assert
        assertThat(sut.getDefault()).isEqualTo(10);
        assertThat(sut.getAll()).containsExactly(10);
        assertThat(sut.get(A)).get().isEqualTo(10);
        assertThat(sut.get(B)).isEmpty();
        assertThat(sut.getId(10)).get().isEqualTo(A);
        assertThat(sut.getId(20)).isEmpty();
        assertThat(sut.next(10)).isEqualTo(10);
        assertThat(sut.nextOrNullIfLast(10)).isNull();
        assertThat(sut.next(20)).isEqualTo(10);
        assertThat(sut.nextOrNullIfLast(20)).isEqualTo(10);
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void shouldNotBeAbleToModifyUnderlyingRegistryList() {
        // Arrange
        final List<Integer> list = sut.getAll();

        // Act & assert
        assertThrows(UnsupportedOperationException.class, () -> list.add(1));
    }

    @Test
    void shouldRegisterAndRetrieve() {
        // Act
        sut.register(B, 20);

        // Assert
        assertThat(sut.getDefault()).isEqualTo(10);
        assertThat(sut.getAll()).containsExactly(10, 20);
        assertThat(sut.get(A)).get().isEqualTo(10);
        assertThat(sut.get(B)).get().isEqualTo(20);
        assertThat(sut.getId(10)).get().isEqualTo(A);
        assertThat(sut.getId(20)).get().isEqualTo(B);
        assertThat(sut.next(10)).isEqualTo(20);
        assertThat(sut.nextOrNullIfLast(10)).isEqualTo(20);
        assertThat(sut.next(20)).isEqualTo(10);
        assertThat(sut.nextOrNullIfLast(20)).isNull();
        assertThat(sut.isEmpty()).isFalse();
    }

    @Test
    void shouldNotRegisterDuplicateId() {
        // Arrange
        sut.register(B, 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(B, 20));
        assertThat(sut.getAll()).containsExactly(10, 20);
    }

    @Test
    void shouldNotRegisterDuplicateValue() {
        // Arrange
        sut.register(B, 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(C, 20));
        assertThat(sut.getAll()).containsExactly(10, 20);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidRegistration() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> sut.register(null, 20));
        assertThrows(NullPointerException.class, () -> sut.register(B, null));
        assertThrows(NullPointerException.class, () -> sut.register(null, null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidDefaults() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new PlatformRegistryImpl<>(null, 20));
        assertThrows(NullPointerException.class, () -> new PlatformRegistryImpl<>(B, null));
        assertThrows(NullPointerException.class, () -> new PlatformRegistryImpl<>(null, null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidNextValues() {
        assertThrows(NullPointerException.class, () -> sut.next(null));
        assertThrows(NullPointerException.class, () -> sut.nextOrNullIfLast(null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidRetrievals() {
        assertThrows(NullPointerException.class, () -> sut.get(null));
        assertThrows(NullPointerException.class, () -> sut.getId(null));
    }
}
