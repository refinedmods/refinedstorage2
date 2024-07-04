package com.refinedmods.refinedstorage.platform.common.support.registry;

import com.refinedmods.refinedstorage.platform.api.support.registry.PlatformRegistry;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformRegistryImplTest {
    private static final ResourceLocation A = ResourceLocation.parse("a");
    private static final ResourceLocation B = ResourceLocation.parse("b");
    private static final ResourceLocation C = ResourceLocation.parse("c");

    PlatformRegistry<Integer> sut;

    @BeforeEach
    void setUp() {
        sut = new PlatformRegistryImpl<>();
    }

    @Test
    void testDefaults() {
        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.get(A)).isEmpty();
        assertThat(sut.getId(10)).isEmpty();
        assertThat(sut.nextOrNullIfLast(10)).isNull();
    }

    @Test
    void shouldNotBeAbleToModifyUnderlyingRegistryList() {
        // Arrange
        final List<Integer> list = sut.getAll();

        // Act & assert
        assertThrows(UnsupportedOperationException.class, () -> list.add(1));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(1));
    }

    @Test
    void shouldRegisterAndRetrieve() {
        // Act
        sut.register(A, 10);
        sut.register(B, 20);

        // Assert
        assertThat(sut.getAll()).containsExactly(10, 20);
        assertThat(sut.get(A)).get().isEqualTo(10);
        assertThat(sut.get(B)).get().isEqualTo(20);
        assertThat(sut.getId(10)).get().isEqualTo(A);
        assertThat(sut.getId(20)).get().isEqualTo(B);
        assertThat(sut.nextOrNullIfLast(10)).isEqualTo(20);
        assertThat(sut.nextOrNullIfLast(20)).isNull();
    }

    @Test
    void shouldNotRegisterDuplicateId() {
        // Arrange
        sut.register(B, 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(B, 20));
        assertThat(sut.getAll()).containsExactly(20);
    }

    @Test
    void shouldNotRegisterDuplicateValue() {
        // Arrange
        sut.register(B, 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register(C, 20));
        assertThat(sut.getAll()).containsExactly(20);
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
    void testInvalidNextValues() {
        assertThrows(NullPointerException.class, () -> sut.nextOrNullIfLast(null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidRetrievals() {
        assertThrows(NullPointerException.class, () -> sut.get(null));
        assertThrows(NullPointerException.class, () -> sut.getId(null));
    }
}
