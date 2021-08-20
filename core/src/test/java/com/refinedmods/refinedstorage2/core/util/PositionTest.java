package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.stream.Stream;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class PositionTest {
    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(Position.class).verify();
    }

    @Test
    void Test_properties() {
        // Act
        Position position = new Position(1, 2, 3);

        // Assert
        assertThat(position.getX()).isEqualTo(1);
        assertThat(position.getY()).isEqualTo(2);
        assertThat(position.getZ()).isEqualTo(3);
    }

    public static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of(new Position(1, 2, 3), Direction.NORTH, new Position(1, 2, 2)),
                Arguments.of(new Position(1, 2, 3), Direction.EAST, new Position(2, 2, 3)),
                Arguments.of(new Position(1, 2, 3), Direction.SOUTH, new Position(1, 2, 4)),
                Arguments.of(new Position(1, 2, 3), Direction.WEST, new Position(0, 2, 3)),
                Arguments.of(new Position(1, 2, 3), Direction.UP, new Position(1, 3, 3)),
                Arguments.of(new Position(1, 2, 3), Direction.DOWN, new Position(1, 1, 3))
        );
    }

    @ParameterizedTest
    @MethodSource("test")
    void Test_offset(Position origin, Direction direction, Position expectedPosition) {
        // Act
        Position position = origin.offset(direction);

        // Assert
        assertThat(position)
                .isEqualTo(expectedPosition)
                .isNotSameAs(expectedPosition)
                .isNotSameAs(origin);
    }
}
