package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeWorldAdapter;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class GraphScannerTest {
    private final GraphScanner<FurnaceBlockEntity, BlockEntityRequest> scanner = new GraphScanner<>(new BlockEntityRequestHandler<>(FurnaceBlockEntity.class));

    @Test
    void Test_scanning_from_origin_contains_origin() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_does_not_connect_to_incompatible_neighbor() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_does_connect_to_compatible_neighbor() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01, b02, b03);
    }

    @Test
    void Test_scanning_blocks_compatible_neighbors_if_incompatible_neighbor_is_in_between() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01);
    }

    @Test
    void Test_scanning_can_still_find_neighbor_with_alternate_route_if_is_blocked() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north(), new FurnaceBlockEntity());
        FurnaceBlockEntity b04 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north().down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b05 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north().down().down(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(b01, b02, b03, b04, b05);
    }

    @Test
    void Test_scanner_can_detect_new_entries() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN), result1.getAllEntries());

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
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b00 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());
        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        worldAdapter.removeBlockEntity(BlockPos.ORIGIN.down());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN), result1.getAllEntries());

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
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b00 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());
        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN));

        worldAdapter.removeBlockEntity(BlockPos.ORIGIN.up());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(new BlockEntityRequest(worldAdapter, BlockPos.ORIGIN), result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(b00, b01);
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(b00, b01);

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(b01, b03, b02);
        assertThat(result2.getRemovedEntries()).containsExactlyInAnyOrder(b00);
        assertThat(result2.getNewEntries()).containsExactlyInAnyOrder(b03, b02);
    }
}
