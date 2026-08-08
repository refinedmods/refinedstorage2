package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitorNodeTypeIdTest {
    @Test
    void shouldCreateWithRandomId() {
        // Act
        final MonitorNodeTypeId id1 = MonitorNodeTypeId.create();
        final MonitorNodeTypeId id2 = MonitorNodeTypeId.create();

        // Assert
        assertThat(id1.id()).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotAllowNullId() {
        // Act & assert
        assertThatThrownBy(() -> new MonitorNodeTypeId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Id must not be null");
    }

    @Test
    void shouldBeEqualForSameId() {
        // Arrange
        final UUID uuid = UUID.randomUUID();

        // Act & assert
        assertThat(new MonitorNodeTypeId(uuid)).isEqualTo(new MonitorNodeTypeId(uuid));
    }
}
