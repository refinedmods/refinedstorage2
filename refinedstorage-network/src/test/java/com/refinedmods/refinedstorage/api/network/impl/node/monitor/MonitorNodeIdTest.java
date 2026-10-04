package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitorNodeIdTest {
    @Test
    void shouldCreateWithRandomId() {
        // Act
        final MonitorNodeId id1 = MonitorNodeId.create();
        final MonitorNodeId id2 = MonitorNodeId.create();

        // Assert
        assertThat(id1.id()).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotAllowNullId() {
        // Act & assert
        assertThatThrownBy(() -> new MonitorNodeId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Id must not be null");
    }

    @Test
    void shouldBeEqualForSameId() {
        // Arrange
        final UUID uuid = UUID.randomUUID();

        // Act & assert
        assertThat(new MonitorNodeId(uuid)).isEqualTo(new MonitorNodeId(uuid));
    }
}
