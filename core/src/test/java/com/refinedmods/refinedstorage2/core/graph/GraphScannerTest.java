package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GraphScannerTest {
    private static final String TYPE_FURNACE = "furnace";
    private static final String TYPE_CHEST = "chest";

    private final GraphScanner<Position, FakeRequest> scanner = new GraphScanner<>(new FakeRequestHandler(TYPE_FURNACE));

    @Test
    void Test_scanning_from_origin_contains_origin() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);

        world.setType(new Position(10, 10, 10), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_does_not_connect_to_incompatible_neighbor() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        world.setType(Position.ORIGIN.down(), TYPE_CHEST);

        world.setType(new Position(10, 10, 10), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_does_connect_to_compatible_neighbor() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        Position b02 = world.setType(Position.ORIGIN.down(), TYPE_FURNACE);
        Position b03 = world.setType(Position.ORIGIN.up(), TYPE_FURNACE);

        world.setType(new Position(10, 10, 10), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01, b02, b03);
    }

    @Test
    void Test_scanning_blocks_compatible_neighbors_if_incompatible_neighbor_is_in_between() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        world.setType(Position.ORIGIN.down(), TYPE_CHEST);
        world.setType(Position.ORIGIN.down().down(), TYPE_FURNACE);

        world.setType(new Position(10, 10, 10), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_can_still_find_neighbor_with_alternate_route_if_is_blocked() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        world.setType(Position.ORIGIN.down(), TYPE_CHEST);
        Position b02 = world.setType(Position.ORIGIN.down().down(), TYPE_FURNACE);
        Position b03 = world.setType(Position.ORIGIN.north(), TYPE_FURNACE);
        Position b04 = world.setType(Position.ORIGIN.north().down(), TYPE_FURNACE);
        Position b05 = world.setType(Position.ORIGIN.north().down().down(), TYPE_FURNACE);

        world.setType(new Position(10, 10, 10), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01, b02, b03, b04, b05);
    }

    @Test
    void Test_scanner_can_detect_new_entries() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result1 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        Position b02 = world.setType(Position.ORIGIN.down(), TYPE_FURNACE);

        GraphScannerResult<Position> result2 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN), result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(b01);
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(b01);

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(b01, b02);
        assertThat(result2.getRemovedEntries()).isEmpty();
        assertThat(result2.getNewEntries()).containsExactlyInAnyOrder(b02);
    }

    @Test
    void Test_scanner_can_detect_removed_entries() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b00 = world.setType(Position.ORIGIN.up(), TYPE_FURNACE);
        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        Position b02 = world.setType(Position.ORIGIN.down(), TYPE_FURNACE);
        Position b03 = world.setType(Position.ORIGIN.down().down(), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result1 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        world.removeType(Position.ORIGIN.down());

        GraphScannerResult<Position> result2 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN), result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(b00, b01, b02, b03);
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(b00, b01, b02, b03);

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(b00, b01);
        assertThat(result2.getRemovedEntries()).containsExactlyInAnyOrder(b02, b03);
        assertThat(result2.getNewEntries()).isEmpty();
    }

    @Test
    void Test_scanner_can_detect_new_and_removed_entries() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        Position b00 = world.setType(Position.ORIGIN.up(), TYPE_FURNACE);
        Position b01 = world.setType(Position.ORIGIN, TYPE_FURNACE);
        Position b02 = world.setType(Position.ORIGIN.down().down(), TYPE_FURNACE);

        // Act
        GraphScannerResult<Position> result1 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN));

        world.removeType(Position.ORIGIN.up());
        Position b03 = world.setType(Position.ORIGIN.down(), TYPE_FURNACE);

        GraphScannerResult<Position> result2 = scanner.scanAt(new FakeRequest(world, Position.ORIGIN), result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(b00, b01);
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(b00, b01);

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(b01, b03, b02);
        assertThat(result2.getRemovedEntries()).containsExactlyInAnyOrder(b00);
        assertThat(result2.getNewEntries()).containsExactlyInAnyOrder(b03, b02);
    }
}
